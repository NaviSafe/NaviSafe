import json
import logging
from pyspark.sql.functions import *
from pyspark.sql.types import *
from utils.spark_session import get_spark
from utils.redis_utils import RedisClient

log = logging.getLogger("airflow.task")

# -------------------------------------------------
# Redis 설정
# -------------------------------------------------
redis_client = RedisClient(host="redis", port=6379, db=0)

REDIS_QUEUE_KEY = "emergency_alert_queue"
REDIS_PUB_CHANNEL = "EMERGENCY_ALERT_CHANNEL"

# -------------------------------------------------
# Spark Batch 처리 함수
# -------------------------------------------------
def process_emergency_alert_batch(batch_df, batch_id):
    log.info(f"[BATCH START] Emergency Alert Batch ID: {batch_id}")

    if batch_df.count() == 0:
        log.info("[INFO] Empty batch - skip")
        return

    batch_df.show(truncate=False)

    for row in batch_df.collect():
        alert = row.asDict()

        # Redis Queue (MySQL 적재용)
        redis_client.rpush_list(REDIS_QUEUE_KEY, alert)

        # Redis Pub/Sub (실시간 알림용)
        redis_client.publish_channel(
            REDIS_PUB_CHANNEL,
            {
                "message": alert["MSG_CN"],
                "region": alert["RCPTN_RGN_NM"],
                "level": alert["EMRG_STEP_NM"],
                "type": alert["DST_SE_NM"],
                "created_at": alert["CRT_DT"]
            }
        )

    log.info(f"[BATCH END] Batch {batch_id} → Redis 저장 완료")

# -------------------------------------------------
# Spark Streaming 실행
# -------------------------------------------------
def run_emergency_alert_streaming():
    log.info("[SYSTEM] Emergency Alert Spark Streaming 시작")

    spark = get_spark(app_name="EmergencyAlertConsumer")

    # ---------------------------------------------
    # 긴급재난문자 스키마 (API 기준)
    # ---------------------------------------------
    alert_schema = StructType([
        StructField("SN", StringType(), True),
        StructField("CRT_DT", StringType(), True),
        StructField("MSG_CN", StringType(), True),
        StructField("RCPTN_RGN_NM", StringType(), True),
        StructField("EMRG_STEP_NM", StringType(), True),
        StructField("DST_SE_NM", StringType(), True),
        StructField("REG_YMD", StringType(), True),
        StructField("MDFCN_YMD", StringType(), True),
    ])

    # ---------------------------------------------
    # Kafka → Spark
    # ---------------------------------------------
    df_stream = (
        spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("subscribe", "emergency_alert_topic")
        .option("startingOffsets", "latest")
        .load()
    )

    df_parsed = (
        df_stream
        .selectExpr("CAST(value AS STRING) AS json_str")
        .select(from_json(col("json_str"), alert_schema).alias("data"))
        .select("data.*")
    )

    # ---------------------------------------------
    # Streaming 실행
    # ---------------------------------------------
    query = (
        df_parsed.writeStream
        .foreachBatch(process_emergency_alert_batch)
        .outputMode("append")
        .option("checkpointLocation", "/tmp/emergency_alert_checkpoint")
        .start()
    )

    log.info("[SYSTEM] Emergency Alert Streaming Query 실행 완료")
    query.awaitTermination()

# -------------------------------------------------
# main
# -------------------------------------------------
if __name__ == "__main__":
    run_emergency_alert_streaming()