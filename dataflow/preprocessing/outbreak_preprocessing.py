import os
import time
import json
import requests
import subprocess
import mysql.connector
import xml.etree.ElementTree as ET
from pyspark.sql.functions import *
from pyspark.sql.types import StructType, StructField, StringType, ArrayType
from utils.spark_session import get_spark
from utils.mysql_utils import process_batch
from utils.redis_utils import RedisClient
import logging

# -----------------------------------
# 로깅 설정
# -----------------------------------
log = logging.getLogger("airflow.task")

# -----------------------------------
# Redis 클라이언트 초기화
# -----------------------------------
redis_client = RedisClient(host="redis", port=6379, db=0)
DB_BATCH_SIZE = 100  # Redis→MySQL 저장 단위


# -----------------------------------
# Redis 저장 + DB 큐 처리 함수
# -----------------------------------
def process_batch_with_redis(batch_df, batch_id):
    log.info(f"--- 배치 {batch_id} 시작 ---")
    batch_df.show(truncate=False)
    
    for row in batch_df.collect():
        item = row.asDict()
        link_id = item.get("link_id")
        acc_id = item.get("acc_id")

        # 지도 좌표(중복 체크 포함)
        gps_key = f"gps_sent:{item['acc_id']}"
        if not redis_client.r.exists(gps_key):
            redis_client.publish_channel("MAP_GPS", {
                "acc_id": item["acc_id"],
                "x": item["grs80tm_x"],
                "y": item["grs80tm_y"],
                "acc_info" : item["acc_info"],
                'exp_clr_date_time': item['exp_clr_date_time'] 
            })
            redis_client.r.set(gps_key, 1, ex=3600)
        else:
            log.info(f"[INFO] Duplicate gps skipped for ACC_ID: {item['acc_id']}")

        # 실시간 알림 발행 (중복 체크 포함)
        alert_key = f"alert_sent:{item['acc_id']}"
        if not redis_client.r.exists(alert_key):
            redis_client.publish_channel("ACC_ALERTS", {
                "acc_id": item["acc_id"],
                "occr_date_time": item["occr_date_time"],
                "exp_clr_date_time": item["exp_clr_date_time"],
                "acc_info": item["acc_info"]
            })
            redis_client.r.set(alert_key, 1, ex=3600)
        else:
            log.info(f"[INFO] Duplicate alert skipped for ACC_ID: {item['acc_id']}")

        # MySQL 저장용 데이터 (db_queue)
        if link_id and acc_id:
            redis_client.rpush_list("db_queue", item)
        else:
            log.warning(f"[SKIP] acc_id 없음 → db_queue에 저장 안함: {item}")

        # LinkInfo API 호출용 데이터 (link_queue)

        if link_id and acc_id:
            if link_id and not redis_client.r.exists(f"link_sent:{link_id}"):
                redis_client.rpush_list("link_queue", {"link_id": link_id})
                redis_client.r.set(f"link_sent:{link_id}", 1, ex=3600)
                log.info(f"[INFO] link_queue에 LINK_ID 추가됨: {link_id}")


# -----------------------------------
# Redis → MySQL 주기적 배치 저장
# -----------------------------------
def save_from_redis_to_mysql():
    batch = []
    for _ in range(DB_BATCH_SIZE):
        item = redis_client.lpop("db_queue")
        if item is None:
            break
        batch.append(json.loads(item))

    if not batch:
        log.info("[INFO] No data in Redis, sleeping 5s...")
        time.sleep(5)
        return

    unique_batch = list({
    d["acc_id"]: d
    for d in batch
    if d.get("link_id")  # link_id가 존재하는 경우만
    }.values())


    conn = mysql.connector.connect(
        host="mysql",
        user="user",
        password="userpass",
        database="toy_project"
    )
    cursor = conn.cursor()

    # OUTBREAK_OCCURRENCE
    try:
        cursor.executemany("""
            INSERT IGNORE INTO OUTBREAK_OCCURRENCE (ACC_ID, occr_date_time, exp_clr_date_time)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE
                occr_date_time=VALUES(occr_date_time),
                exp_clr_date_time=VALUES(exp_clr_date_time)
        """, [(d["acc_id"], d.get("occr_date_time"), d.get("exp_clr_date_time")) for d in unique_batch])
        log.info(f"[DB] OUTBREAK_OCCURRENCE 삽입 완료 ({len(unique_batch)}건)")
    except Exception as e:
        log.error(f"[ERROR] OUTBREAK_OCCURRENCE 삽입 실패: {e}")

    # OUTBREAK_DETAIL_CODE
    try:
        cursor.execute("SELECT ACC_DTYPE FROM OUTBREAK_DETAIL_CODE_NAME")
        valid_dtypes = {r[0] for r in cursor.fetchall()}
        batch_detail = [d for d in unique_batch if d["acc_dtype"] in valid_dtypes]
        if batch_detail:
            cursor.executemany("""
                INSERT IGNORE INTO OUTBREAK_DETAIL_CODE (OUTBREAK_ACC_ID, ACC_DTYPE)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE ACC_DTYPE=VALUES(ACC_DTYPE)
            """, [(d["acc_id"], d["acc_dtype"]) for d in batch_detail])
            log.info(f"[DB] OUTBREAK_DETAIL_CODE 삽입 완료 ({len(batch_detail)}건)")
    except Exception as e:
        log.error(f"[ERROR] OUTBREAK_DETAIL_CODE 삽입 실패: {e}")

    # linkinfo_worker.py 실행
    log.info("[SYSTEM] linkinfo_worker.py 실행 중...")
    env = os.environ.copy()
    try:
        result = subprocess.run(

            ["python3", "/app/preprocessing/linkinfo_worker.py"],
            capture_output=True, text=True, timeout=30, env=env
        )

        log.info(f"[SYSTEM] linkinfo_worker.py 종료 코드: {result.returncode}")
        if result.stdout:
            log.info("[STDOUT]\n" + result.stdout)
        if result.stderr:
            log.error("[STDERR]\n" + result.stderr)

        if result.returncode != 0:
            log.error(f"[ERROR] linkinfo_worker.py 비정상 종료 (code={result.returncode})")

    except subprocess.TimeoutExpired:
        log.error("[ERROR] linkinfo_worker.py 실행 시간 초과")
    except Exception as e:
        log.error(f"[ERROR] linkinfo_worker.py 실행 실패: {e}")

    # OUTBREAK_LINK
    try:
        cursor.execute("SELECT LINK_ID FROM LINK_ID")
        valid_links = {r[0] for r in cursor.fetchall()}
        batch_link = [d for d in unique_batch if d["link_id"] in valid_links]
        if batch_link:
            cursor.executemany("""
                INSERT INTO OUTBREAK_LINK (OUTBREAK_ACC_ID, LINK_ID)
                VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE
                    OUTBREAK_ACC_ID = VALUES(OUTBREAK_ACC_ID),
                    LINK_ID = VALUES(LINK_ID)
            """, [(d["acc_id"], d["link_id"]) for d in batch_link])
            log.info(f"[DB] OUTBREAK_LINK 삽입 완료 ({len(batch_link)}건)")
    except Exception as e:
        log.error(f"[ERROR] OUTBREAK_LINK 삽입 실패: {e}")

    # 나머지 테이블 삽입
    try:
        cursor.executemany("""
            INSERT IGNORE INTO OUTBREAK_CODE (OUTBREAK_ACC_ID, ACC_TYPE)
            VALUES (%s, %s)
            ON DUPLICATE KEY UPDATE ACC_TYPE=VALUES(ACC_TYPE)
        """, [(d["acc_id"], d["acc_type"]) for d in unique_batch])

        cursor.executemany("""
            INSERT IGNORE INTO MAP_GPS (OUTBREAK_ACC_ID, GRS80TM_X, GRS80TM_Y)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE
                GRS80TM_X=VALUES(GRS80TM_X),
                GRS80TM_Y=VALUES(GRS80TM_Y)
        """, [(d["acc_id"], d["grs80tm_x"], d["grs80tm_y"]) for d in unique_batch])

        cursor.executemany("""
            INSERT IGNORE INTO ACC_ALERTS (OUTBREAK_ACC_ID, ACC_INFO)
            VALUES (%s, %s)
            ON DUPLICATE KEY UPDATE ACC_INFO=VALUES(ACC_INFO)
        """, [(d["acc_id"], d["acc_info"]) for d in unique_batch])

        log.info("[DB] MAP_GPS / ACC_ALERTS / OUTBREAK_CODE 삽입 완료")
    except Exception as e:
        log.error(f"[ERROR] 나머지 테이블 삽입 실패: {e}")

    conn.commit()
    cursor.close()
    conn.close()
    log.info("[SYSTEM] Redis → MySQL 배치 저장 완료.")


# -----------------------------------
# PySpark 스트리밍 실행 함수
# -----------------------------------
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
        StructField("acc_info", StringType(), True)
    ])
    schema = ArrayType(inner_schema)

    df_stream = spark.readStream.format("kafka") \
        .option("kafka.bootstrap.servers", "kafka:9092") \
        .option("subscribe", "outbreak_topic") \
        .option("startingOffsets", "earliest") \
        .load()

    df_json = df_stream.selectExpr("CAST(value AS STRING) as json_str") \
        .select(from_json(col("json_str"), schema).alias("data")) \
        .select(explode(col("data")).alias("data")) \
        .select("data.*")

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
    ).drop("occr_date", "occr_time", "exp_clr_date", "exp_clr_time")

    query = df_json.writeStream \
        .foreachBatch(process_batch_with_redis) \
        .outputMode("append") \
        .option("checkpointLocation", "/tmp/weatherflow_checkpoint") \
        .start()

    log.info("[SYSTEM] Spark Streaming 쿼리 시작 완료")
    query.awaitTermination()


if __name__ == "__main__":
    run_outbreak_streaming()
