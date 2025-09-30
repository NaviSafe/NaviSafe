from pyspark.sql.functions import *
from pyspark.sql.types import StructType, StructField, StringType, ArrayType
from utils.spark_session import get_spark
from utils.mysql_utils import process_batch
from utils.redis_utils import RedisClient
import threading
import time
import mysql.connector
import json

# SparkSession 생성 (Kafka 패키지 포함)
spark = get_spark(app_name="OutbreakConsumer")
redis_client = RedisClient(host="redis", port=6379, db=0)

# 스키마 (배열 안에 객체)
inner_schema = StructType([
    StructField("acc_id", StringType(), True),
    StructField("link_id", StringType(), True),
    StructField("occr_date", StringType(), True),
    StructField("occr_time", StringType(), True),
    StructField("exp_clr_date", StringType(), True),
    StructField("exp_clr_time", StringType(), True),
    StructField("acc_type", StringType(), True),
    StructField("acc_dtype", StringType(), True),
    StructField("acc_info", StringType(), True),
    StructField("grs80tm_x", StringType(), True),
    StructField("grs80tm_y", StringType(), True),
    StructField("acc_road_code", StringType(), True),
])

schema = ArrayType(inner_schema)

df_stream = spark.readStream.format("kafka") \
    .option("kafka.bootstrap.servers", "kafka:9092") \
    .option("subscribe", "outbreak_topic") \
    .option("startingOffsets", "earliest") \
    .load()


## 현재 들어오는 값들 출력
# df_stream.selectExpr("CAST(value AS STRING)").writeStream \
#     .format("console") \
#     .start()

# JSON 파싱 + explode
df_json = df_stream.selectExpr("CAST(value AS STRING) as json_str") \
    .select(from_json(col("json_str"), schema).alias("data")) \
    .select(explode(col("data")).alias("data")) \
    .select("data.*")


# 날짜/시간 필드 변환 (자동 생성된 컬럼을 사용)
df_json = df_json.withColumn(
    "occr_date_time",
    date_format(
        to_timestamp(concat_ws(" ", col("occr_date"), col("occr_time")), "yyyyMMdd HHmm"),
        "yyyy-MM-dd HH:mm:ss"
    )
).withColumn(
    "exp_clr_date_time",
    date_format(
        when(length(col("exp_clr_time")) == 4,
             to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmm"))
        .otherwise(to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmmss")),
        "yyyy-MM-dd HH:mm:ss"
    )
)

# 기존 컬럼 삭제
df_json = df_json.drop("occr_date", "occr_time", "exp_clr_date", "exp_clr_time")

# -----------------------------------
# Redis 저장 + DB 큐 처리 함수
# -----------------------------------
def process_batch_with_redis(batch_df, batch_id):
    print(f"--- 배치 {batch_id} ---")
    batch_df.show(truncate=False)

    for row in batch_df.collect():
        item = row.asDict()

        # 1) 지도 좌표
        redis_client.rpush_list("map_coords", {
            "acc_id": item["acc_id"],
            "x": item["grs80tm_x"],
            "y": item["grs80tm_y"]
        })

        # 2) 실시간 알림 발행
        redis_client.publish_channel("acc_alerts", {
            "occr_date_time": item["occr_date_time"],
            "exp_clr_date_time": item["exp_clr_date_time"],
            "acc_info": item["acc_info"]
        })

        # 3) MySQL 저장용 데이터 (전체 컬럼 포함)
        redis_client.rpush_list("db_queue", item)  # item은 inner_schema 전체 컬럼

# -----------------------------------
# Redis → MySQL 주기적 배치 저장
# -----------------------------------
DB_BATCH_SIZE = 100  # Redis에서 한 번에 MySQL로 저장할 개수

def save_from_redis_to_mysql():
    while True:
        batch = []
        for _ in range(DB_BATCH_SIZE):
            item = redis_client.lpop("db_queue")
            if item is None:
                break
            batch.append(json.loads(item))  # Redis에서 가져온 JSON → dict
    
        if batch:
            unique_batch = list({d["acc_id"]: d for d in batch}.values())
            print(f"[INFO] Batch size: {len(batch)}")
            print(f"[INFO] Sample data: {batch[0]}")
            
            # MySQL에 batch insert
            conn = mysql.connector.connect(
                host="mysql",
                user="user",
                password="userpass",
                database="toy_project"
            )
            cursor = conn.cursor()

            cursor.executemany("""
                INSERT INTO OUTBREAK_Occurrence (acc_id, occr_date_time, exp_clr_date_time)
                VALUES (%s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    occr_date_time=VALUES(occr_date_time),
                    exp_clr_date_time=VALUES(exp_clr_date_time)
            """, [(d["acc_id"], d["occr_date_time"], d["exp_clr_date_time"]) for d in unique_batch])


            conn.commit()
            cursor.close()
            conn.close()
        else:
            print("[INFO] No data in Redis, sleeping 5s...")
            time.sleep(5)


# 별도 쓰레드로 실행
threading.Thread(target=save_from_redis_to_mysql, daemon=True).start()

# 스트리밍 실행
query = df_json.writeStream \
    .foreachBatch(process_batch_with_redis) \
    .outputMode("append") \
    .option("checkpointLocation", "/tmp/weatherflow_checkpoint") \
    .start()

# 스트리밍 유지
query.awaitTermination()

