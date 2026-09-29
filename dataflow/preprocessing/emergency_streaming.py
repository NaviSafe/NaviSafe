import logging
from pyspark.sql.functions import col, from_json, explode
from pyspark.sql.types import *
from spark_session import get_spark
from redis_utils import RedisClient
import os
from datetime import datetime

log = logging.getLogger("airflow.task")

# -------------------------------------------------
# Redis 설정
# -------------------------------------------------
redis_client = RedisClient(host="redis.default", port=6379, db=0)

REDIS_QUEUE_KEY = "emergency_alert_queue"   # MySQL 적재용
REDIS_PUB_CHANNEL = "EMERGENCY_ALERT_CHANNEL" # Redis Pub/Sub

def save_batch_to_s3(batch_df, batch_id):
    bucket = os.getenv("S3_BUCKET")
    region = os.getenv("AWS_REGION")

    if not bucket:
        raise ValueError("S3_BUCKET 환경변수가 없습니다.")

    hadoop_conf = batch_df.sparkSession.sparkContext._jsc.hadoopConfiguration()
    hadoop_conf.set("fs.s3a.access.key", os.getenv("AWS_ACCESS_KEY_ID"))
    hadoop_conf.set("fs.s3a.secret.key", os.getenv("AWS_SECRET_ACCESS_KEY"))
    hadoop_conf.set("fs.s3a.endpoint", f"s3.{region}.amazonaws.com")
    hadoop_conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")

    today = datetime.now().strftime("%Y-%m-%d")
    path = f"s3a://{bucket}/processed/emergency_alert/dt={today}/batch_id={batch_id}"

    batch_df.write.mode("append").parquet(path)

    log.info(f"[S3] 저장 완료: {path}")

# -------------------------------------------------
# Spark Batch 처리
# -------------------------------------------------
def process_emergency_alert_batch(batch_df, batch_id):
    log.info(f"[BATCH START] Emergency Alert Batch ID: {batch_id}")

    if batch_df.count() == 0:
        log.info("[INFO] Empty batch")
        return
    
    save_batch_to_s3(batch_df, batch_id)
    batch_df.show(truncate=False)

    for row in batch_df.collect():
        alert = row.asDict()

        # Redis Queue (MySQL 적재용)
        redis_client.rpush_list(REDIS_QUEUE_KEY, alert)

        # Redis Pub/Sub
        redis_client.publish_channel(
            REDIS_PUB_CHANNEL,
            {
                "sn": alert["SN"],
                "message": alert["MSG_CN"],
                "region": alert["RCPTN_RGN_NM"],
                "level": alert["EMRG_STEP_NM"],
                "type": alert["DST_SE_NM"],
                "created_at": alert["CRT_DT"]
            }
        )

    log.info(f"[BATCH END] Batch {batch_id} 완료")

# -------------------------------------------------
# Streaming 실행
# -------------------------------------------------
def run_emergency_alert_streaming():
    log.info("[SYSTEM] Emergency Alert Spark Streaming 시작")

    spark = get_spark(app_name="EmergencyAlertConsumer")

    alert_schema = ArrayType(
        StructType([
            StructField("SN", LongType()),
            StructField("CRT_DT", StringType()),
            StructField("MSG_CN", StringType()),
            StructField("RCPTN_RGN_NM", StringType()),
            StructField("EMRG_STEP_NM", StringType()),
            StructField("DST_SE_NM", StringType()),
            StructField("REG_YMD", StringType()),
            StructField("MDFCN_YMD", StringType()),
        ])
    )
    
    df_stream = (
        spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka-svc:9092")
        .option("subscribe", "emergency_alert_topic")
        .option("startingOffsets", "latest")
        .load()
    )

    df_parsed = (
        df_stream
        .selectExpr("CAST(value AS STRING) AS json_str")
        .select(from_json(col("json_str"), alert_schema).alias("data"))
        .select(explode(col("data")).alias("alert"))
        .select("alert.*")
    )

    query = (
        df_parsed.writeStream
        .foreachBatch(process_emergency_alert_batch)
        .outputMode("append")
        .option("checkpointLocation", "/tmp/emergency_alert_checkpoint") 
        .start()
    )

    log.info("[SYSTEM] Streaming Query started")
    log.info(f"[SYSTEM] Query ID: {query.id}")
    log.info(f"[SYSTEM] Is Active: {query.isActive}")
    query.awaitTermination()

# -------------------------------------------------
# main
# -------------------------------------------------
if __name__ == "__main__":
    run_emergency_alert_streaming()
