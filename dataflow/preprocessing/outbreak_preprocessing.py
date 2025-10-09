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
    StructField("ACC_DTYPE", StringType(), True),
    StructField("ACC_DTYPE_NM", StringType(), True),
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
        # -----------------------------
        # 1) 지도 좌표(중복 체크 포함)
        # -----------------------------
        gps_key = f"gps_sent:{item['acc_id']}"
        if not redis_client.r.exists(gps_key):
            redis_client.publish_channel("MAP_GPS", {
                "acc_id": item["acc_id"],
                "x": item["grs80tm_x"],
                "y": item["grs80tm_y"]
            })
            redis_client.r.set(gps_key, 1, ex=3600)
        else:
            print(f"[INFO] Duplicate gps skipped for ACC_ID: {item['acc_id']}")
        
        # -----------------------------
        # 2) 실시간 알림 발행 (중복 체크 포함)
        # -----------------------------
        alert_key = f"alert_sent:{item['acc_id']}"
        if not redis_client.r.exists(alert_key):
            redis_client.publish_channel("ACC_ALTERTS", {
                "acc_id": item["acc_id"],
                "occr_date_time": item["occr_date_time"],
                "exp_clr_date_time": item["exp_clr_date_time"],
                "acc_info": item["acc_info"]
            })
            # 중복 방지 키 생성, TTL 1시간
            redis_client.r.set(alert_key, 1, ex=3600)
        else:
            print(f"[INFO] Duplicate alert skipped for ACC_ID: {item['acc_id']}")

        # -----------------------------
        # 3) MySQL 저장용 데이터 (전체 컬럼 포함)
        # -----------------------------
        redis_client.rpush_list("db_queue", item)  # item은 inner_schema 전체 컬럼

        # -----------------------------
        # 4) LinkInfo, TrafficInfo API 호출용 데이터 (link_queue)
        # -----------------------------
        link_id = item.get("link_id")
        if link_id and not redis_client.r.exists(f"link_sent:{link_id}"):
            redis_client.rpush_list("link_queue", {"link_id": link_id})
            redis_client.r.set(f"link_sent:{link_id}", 1, ex=3600)
            print(f"[INFO] link_queue에 LINK_ID 추가됨: {link_id}")


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
                INSERT INTO OUTBREAK_Occurrence (ACC_ID, occr_date_time, exp_clr_date_time)
                VALUES (%s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    occr_date_time=VALUES(occr_date_time),
                    exp_clr_date_time=VALUES(exp_clr_date_time)
            """, [(d["acc_id"], d["occr_date_time"], d["exp_clr_date_time"]) for d in unique_batch])

            cursor.executemany("""
                INSERT INTO OUTBREAK_CODE (OUTBREAK_ACC_ID, ACC_TYPE)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE
                    ACC_TYPE=VALUES(ACC_TYPE)
            """, [(d["acc_id"], d["acc_type"]) for d in unique_batch])

            cursor.executemany("""
                INSERT INTO OUTBREAK_DETAIL_CODE (OUTBREAK_ACC_ID , ACC_DTYPE)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE
                    ACC_DTYPE=VALUES(ACC_DTYPE)
            """, [(d["acc_id"], d["acc_dtype"]) for d in unique_batch])

            cursor.executemany("""
                INSERT INTO MAP_GPS (OUTBREAK_ACC_ID , GRS80TM_X, GRS80TM_Y)
                VALUES (%s, %s, %s)
                ON DUPLICATE KEY UPDATE
                    GRS80TM_X=VALUES(GRS80TM_X),
                    GRS80TM_Y=VALUES(GRS80TM_Y),         
            """, [(d["acc_id"], d["acc_dtype"]) for d in unique_batch])

            cursor.executemany("""
                INSERT INTO ACC_ALTERTS (OUTBREAK_ACC_ID , ACC_INFO)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE
                    ACC_INFO=VALUES(ACC_INFO)
            """, [(d["acc_id"], d["acc_info"]) for d in unique_batch])

            cursor.executemany("""
                INSERT INTO OUTBREAK_LINK (OUTBREAK_ACC_ID , LINK_ID)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE
                    LINK_ID=VALUES(LINK_ID)
            """, [(d["acc_id"], d["link_id "]) for d in unique_batch])            


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

