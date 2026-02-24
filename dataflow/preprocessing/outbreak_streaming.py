import os
import logging

from pyspark.sql.functions import *
from pyspark.sql.types import *

from utils.spark_session import get_spark
from utils.redis_utils import RedisClient

log = logging.getLogger("outbreak-streaming")
log.setLevel(logging.INFO)


def process_batch_with_redis(batch_df, batch_id):
    log.info(f"--- 배치 {batch_id} 시작 ---")

    if batch_df.rdd.isEmpty():
        log.info("빈 배치 → 처리 생략")
        return

    # ⚠ 반드시 함수 내부에서 생성 (직렬화 문제 방지)
    redis_client = RedisClient(
        host=os.getenv("REDIS_HOST", "redis"),
        port=6379,
        db=0
    )

    for row in batch_df.toLocalIterator():
        item = row.asDict()

        link_id = item.get("link_id")
        acc_id = item.get("acc_id")

        if not acc_id:
            log.warning(f"[SKIP] acc_id 없음: {item}")
            continue

        # ======================
        # MAP_GPS 중복 방지 발행
        # ======================
        gps_key = f"gps_sent:{acc_id}"

        if not redis_client.r.exists(gps_key):
            redis_client.publish_channel("MAP_GPS", {
                "acc_id": acc_id,
                "x": item.get("grs80tm_x"),
                "y": item.get("grs80tm_y"),
                "acc_info": item.get("acc_info"),
                "exp_clr_date_time": item.get("exp_clr_date_time")
            })
            redis_client.r.set(gps_key, 1, ex=3600)

        # ======================
        # ACC_ALERTS 발행
        # ======================
        alert_key = f"alert_sent:{acc_id}"

        if not redis_client.r.exists(alert_key):
            redis_client.publish_channel("ACC_ALERTS", {
                "acc_id": acc_id,
                "occr_date_time": item.get("occr_date_time"),
                "exp_clr_date_time": item.get("exp_clr_date_time"),
                "acc_info": item.get("acc_info")
            })
            redis_client.r.set(alert_key, 1, ex=3600)

        # ======================
        # MySQL 저장용 큐
        # ======================
        if link_id:
            redis_client.rpush_list("db_queue", item)

        # ======================
        # Link API 호출용 큐
        # ======================
        if link_id and not redis_client.r.exists(f"link_sent:{link_id}"):
            redis_client.rpush_list("link_queue", {"link_id": link_id})
            redis_client.r.set(f"link_sent:{link_id}", 1, ex=3600)


def run_outbreak_streaming():
    log.info("[SYSTEM] Spark Streaming 시작")

    spark = get_spark(app_name="OutbreakConsumer")

    # Kafka JSON 배열 구조
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
        StructField("acc_info", StringType(), True)
    ])

    schema = ArrayType(inner_schema)

    kafka_bootstrap = os.getenv("KAFKA_BOOTSTRAP", "kafka-svc:9092")

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

    # ======================
    # 날짜 파싱 (4자리/6자리 혼합 대응)
    # ======================
    df_json = (
        df_json
        .withColumn(
            "occr_date_time",
            date_format(
                when(length(col("occr_time")) == 4,
                     to_timestamp(concat_ws(" ", col("occr_date"), col("occr_time")), "yyyyMMdd HHmm"))
                .otherwise(
                     to_timestamp(concat_ws(" ", col("occr_date"), col("occr_time")), "yyyyMMdd HHmmss")
                ),
                "yyyy-MM-dd HH:mm:ss"
            )
        )
        .withColumn(
            "exp_clr_date_time",
            date_format(
                when(length(col("exp_clr_time")) == 4,
                     to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmm"))
                .otherwise(
                     to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmmss")
                ),
                "yyyy-MM-dd HH:mm:ss"
            )
        )
        .drop("occr_date", "occr_time", "exp_clr_date", "exp_clr_time")
    )

    query = (
        df_json.writeStream
        .foreachBatch(process_batch_with_redis)
        .outputMode("append")
        .option("checkpointLocation", "/tmp/outbreak_checkpoint")
        .start()
    )

    log.info("[SYSTEM] Streaming 쿼리 시작 완료")
    query.awaitTermination()


if __name__ == "__main__":
    run_outbreak_streaming()