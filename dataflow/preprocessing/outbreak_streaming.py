import os
import logging

from pyspark.sql.functions import col, explode, from_json, when, length, to_timestamp, concat_ws, date_format
from pyspark.sql.types import StructType, StructField, StringType, ArrayType

from spark_session import get_spark
from redis_utils import RedisClient
from datetime import datetime

log = logging.getLogger("outbreak-streaming")
log.setLevel(logging.INFO)

def save_batch_to_s3(batch_df, batch_id):
    try:
        bucket = os.getenv("S3_BUCKET")
        region = os.getenv("AWS_REGION")

        if not bucket:
            raise ValueError("S3_BUCKET 환경변수 없음")

        # S3 설정
        hadoop_conf = batch_df.sparkSession.sparkContext._jsc.hadoopConfiguration()
        hadoop_conf.set("fs.s3a.access.key", os.getenv("AWS_ACCESS_KEY_ID"))
        hadoop_conf.set("fs.s3a.secret.key", os.getenv("AWS_SECRET_ACCESS_KEY"))
        hadoop_conf.set("fs.s3a.endpoint", f"s3.{region}.amazonaws.com")
        hadoop_conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
        today = datetime.now().strftime("%Y-%m-%d")
        path = f"s3a://{bucket}/processed/outbreak/date={today}"

        batch_df.write.mode("append").parquet(path)

        log.info(f"S3 저장 완료: {path}")

    except Exception as e:
        log.error(f"S3 저장 실패: {e}", exc_info=True)
        raise

def process_batch(batch_df, batch_id):
    log.info(f"--- 배치 {batch_id} 시작 ---")

    try:
        if batch_df.limit(1).count() == 0:
            log.info("빈 배치 → 처리 생략")
            return
    except Exception as e:
        log.error(f"빈 배치 확인 중 오류 발생: {e}", exc_info=True)
        raise
    
    # S3 저장
    save_batch_to_s3(batch_df, batch_id)

    try:
        redis_client = RedisClient(
            host=os.getenv("REDIS_HOST", "redis"),
            port=6379,
            db=0
        )
    except Exception as e:
        log.error(f"Redis 연결 실패: {e}", exc_info=True)
        raise

    try:
        rows = batch_df.collect()
    except Exception as e:
        log.error(f"배치 collect 실패: {e}", exc_info=True)
        raise

    log.info(f"배치 {batch_id} row 수: {len(rows)}")

    for row in rows:
        try:
            item = row.asDict()

            link_id = item.get("link_id")
            acc_id = item.get("acc_id")

            if not acc_id:
                log.warning(f"[SKIP] acc_id 없음: {item}")
                continue

            gps_key = f"gps_sent:{acc_id}"
            if not redis_client.r.exists(gps_key):
                redis_client.publish_channel(
                    "MAP_GPS",
                    {
                        "acc_id": acc_id,
                        "x": item.get("grs80tm_x"),
                        "y": item.get("grs80tm_y"),
                        "acc_info": item.get("acc_info"),
                        "exp_clr_date_time": item.get("exp_clr_date_time"),
                    },
                )
                redis_client.r.set(gps_key, 1, ex=3600)

            alert_key = f"alert_sent:{acc_id}"
            if not redis_client.r.exists(alert_key):
                redis_client.publish_channel(
                    "ACC_ALERTS",
                    {
                        "acc_id": acc_id,
                        "occr_date_time": item.get("occr_date_time"),
                        "exp_clr_date_time": item.get("exp_clr_date_time"),
                        "acc_info": item.get("acc_info"),
                    },
                )
                redis_client.r.set(alert_key, 1, ex=3600)

            if link_id:
                redis_client.rpush_list("db_queue", item)

            if link_id and not redis_client.r.exists(f"link_sent:{link_id}"):
                redis_client.rpush_list("link_queue", {"link_id": link_id})
                redis_client.r.set(f"link_sent:{link_id}", 1, ex=3600)

        except Exception as e:
            log.error(f"row 처리 실패: {e}, row={row}", exc_info=True)
            continue

    log.info(f"--- 배치 {batch_id} 종료 ---")


def run_outbreak_streaming():
    log.info("[SYSTEM] Spark Streaming 시작")

    spark = get_spark(app_name="OutbreakConsumer")

    inner_schema = StructType([
        StructField("acc_id", StringType(), True),
        StructField("occr_date", StringType(), True),
        StructField("occr_time", StringType(), True),
        StructField("exp_clr_date", StringType(), True),
        StructField("exp_clr_time", StringType(), True),
        StructField("acc_type", StringType(), True),
        StructField("acc_dtype", StringType(), True),
        StructField("link_id", StringType(), True),
        StructField("grs80tm_x", StringType(), True),
        StructField("grs80tm_y", StringType(), True),
        StructField("acc_road_code", StringType(), True),
        StructField("acc_info", StringType(), True),
    ])

    schema = ArrayType(inner_schema)

    kafka_bootstrap = os.getenv("KAFKA_BOOTSTRAP", "kafka-svc.default:9092")

    df_stream = (
        spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", kafka_bootstrap)
        .option("subscribe", "outbreak_topic")
        .option("startingOffsets", "earliest")
        .load()
    )

    df_json = (
        df_stream
        .selectExpr("CAST(value AS STRING) as json_str")
        .select(from_json(col("json_str"), schema).alias("data"))
        .select(explode(col("data")).alias("data"))
        .select("data.*")
    )

    df_json = (
        df_json
        .withColumn(
            "occr_date_time",
            date_format(
                when(
                    length(col("occr_time")) == 4,
                    to_timestamp(
                        concat_ws(" ", col("occr_date"), col("occr_time")),
                        "yyyyMMdd HHmm"
                    )
                ).otherwise(
                    to_timestamp(
                        concat_ws(" ", col("occr_date"), col("occr_time")),
                        "yyyyMMdd HHmmss"
                    )
                ),
                "yyyy-MM-dd HH:mm:ss"
            )
        )
        .withColumn(
            "exp_clr_date_time",
            date_format(
                when(
                    length(col("exp_clr_time")) == 4,
                    to_timestamp(
                        concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")),
                        "yyyyMMdd HHmm"
                    )
                ).otherwise(
                    to_timestamp(
                        concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")),
                        "yyyyMMdd HHmmss"
                    )
                ),
                "yyyy-MM-dd HH:mm:ss"
            )
        )
        .drop("occr_date", "occr_time", "exp_clr_date", "exp_clr_time")
    )

    query = (
        df_json.writeStream
        .foreachBatch(process_batch)
        .outputMode("append")
        .option("checkpointLocation", "/tmp/outbreak_checkpoint")
        .start()
    )

    log.info("[SYSTEM] Streaming 쿼리 시작 완료")
    query.awaitTermination()


if __name__ == "__main__":
    run_outbreak_streaming()