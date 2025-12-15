import time
import json
import logging
from pyspark.sql.functions import *
from pyspark.sql.types import *
from utils.spark_session import get_spark
from utils.redis_utils import RedisClient

log = logging.getLogger("airflow.task")
redis_client = RedisClient(host="redis", port=6379, db=0)

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