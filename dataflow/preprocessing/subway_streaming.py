import json
import logging
from pyspark.sql.functions import *
from pyspark.sql.types import (
    StructType, StructField, StringType, ArrayType
)
from utils.spark_session import get_spark
from utils.redis_utils import RedisClient

log = logging.getLogger("airflow.task")

redis_client = RedisClient(host="redis", port=6379, db=0)


# -----------------------------------
# 배치마다 Redis에 저장 + Pub/Sub 발행
# -----------------------------------
def process_subway_batch(batch_df, batch_id):
    log.info(f"--- Subway 배치 {batch_id} 시작 ---")

    if batch_df.rdd.isEmpty():
        log.info("[INFO] 이번 배치는 데이터 없음")
        return

    batch_df.show(truncate=False)

    rows = batch_df.collect()
    for row in rows:
        item = row.asDict()

        train_no = item.get("trainNo")
        if not train_no:
            log.warning(f"[SKIP] trainNo 없음 → 건너뜀: {item}")
            continue

        # Redis HASH 키 (열차번호 기준)
        key = f"subway:{train_no}"

        # HASH에 넣을 필드 구성
        payload = {
            "subwayId": item.get("subwayId"),        # 노선 ID
            "subwayNm": item.get("subwayNm"),        # 3호선
            "statnId": item.get("statnId"),          # 현재역 ID
            "statnNm": item.get("statnNm"),          # 현재역 이름
            "statnTid": item.get("statnTid"),        # 다음역 ID
            "statnTnm": item.get("statnTnm"),        # 다음역 이름
            "updnLine": item.get("updnLine"),        # 상/하행 구분
            "trainSttus": item.get("trainSttus"),    # 상태
            "directAt": item.get("directAt"),        # 급행 여부
            "lstcarAt": item.get("lstcarAt"),        # 막차 여부
            "recptnDt": item.get("recptnDt"),        # 수신 시간
            "lastRecptnDt": item.get("lastRecptnDt"),
            "rowNum": item.get("rowNum"),             
            "selectedCount": item.get("selectedCount"),
            "totalCount": item.get("totalCount")
        }

        # Redis HASH에 저장(덮어쓰기여서 동일한 trainNo이 쌓이지 않음)
        redis_client.r.hset(key, mapping=payload)
        redis_client.r.expire(key, 120)  # 2분 TTL

        # 실시간 Pub/Sub
        redis_client.publish_channel("SUBWAY_POSITION", {
            "trainNo": train_no,
            **payload
        })

        log.info(f"[REDIS] subway:{train_no} 저장 + SUBWAY_POSITION 발행 완료")


# -----------------------------------
# Spark Streaming 메인 로직
# -----------------------------------
def run_subway_streaming():
    log.info("[SYSTEM] Subway Spark Streaming 시작")
    spark = get_spark(app_name="SubwayPositionConsumer")

    # Kafka 메시지 스키마
    row_schema = StructType([
        StructField("rowNum", StringType(), True),
        StructField("selectedCount", StringType(), True),
        StructField("totalCount", StringType(), True),
        StructField("subwayId", StringType(), True),
        StructField("subwayNm", StringType(), True),
        StructField("statnId", StringType(), True),
        StructField("statnNm", StringType(), True),
        StructField("trainNo", StringType(), True),
        StructField("lastRecptnDt", StringType(), True),
        StructField("recptnDt", StringType(), True),
        StructField("updnLine", StringType(), True),
        StructField("statnTid", StringType(), True),
        StructField("statnTnm", StringType(), True),
        StructField("trainSttus", StringType(), True),
        StructField("directAt", StringType(), True),
        StructField("lstcarAt", StringType(), True)
    ])

    schema = StructType([
        StructField("line", StringType(), True),
        StructField("data", ArrayType(row_schema), True)
    ])

    # Kafka에서 데이터 읽기
    df_stream = spark.readStream.format("kafka") \
        .option("kafka.bootstrap.servers", "kafka:9092") \
        .option("subscribe", "subway_position_topic") \
        .option("startingOffsets", "latest") \
        .load()

    df_json = df_stream.selectExpr("CAST(value AS STRING) as json_str") \
        .select(from_json(col("json_str"), schema).alias("json")) \
        .select("json.line", explode("json.data").alias("row")) \
        .select(
            col("line"),
            col("row.rowNum"),
            col("row.selectedCount"),
            col("row.totalCount"),
            col("row.subwayId"),
            col("row.subwayNm"),
            col("row.statnId"),
            col("row.statnNm"),
            col("row.trainNo"),
            col("row.lastRecptnDt"),
            col("row.recptnDt"),
            col("row.updnLine"),
            col("row.statnTid"),
            col("row.statnTnm"),
            col("row.trainSttus"),
            col("row.directAt"),
            col("row.lstcarAt")
        )

    query = df_json.writeStream \
        .foreachBatch(process_subway_batch) \
        .outputMode("update") \
        .option("checkpointLocation", "/tmp/subway_checkpoint") \
        .start()

    log.info("[SYSTEM] Subway Spark Streaming 쿼리 시작 완료")
    query.awaitTermination()


if __name__ == "__main__":
    run_subway_streaming()
