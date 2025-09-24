from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, col, from_unixtime, to_date, concat_ws, to_timestamp
from pyspark.sql.types import StructType, StructField, StringType, FloatType, IntegerType, DateType

# SparkSession 생성 (Kafka 패키지 포함)
spark = SparkSession.builder \
    .appName("WeatherFlowConsumerToMySQL") \
    .config(
        "spark.jars.packages",
        "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.5,"
        "mysql:mysql-connector-java:8.0.33"
    ) \
    .getOrCreate()


# Kafka에서 읽어올 데이터의 스키마 정의
schema = StructType([
    StructField("acc_id", StringType(), True),
    StructField("link_id", StringType(), True),
    StructField("occr_date", StringType(), True),
    StructField("occr_time", StringType(), True),
    StructField("exp_clr_date", StringType(), True),
    StructField("exp_clr_time", StringType(), True),
    StructField("acc_type", StringType(), True),
    StructField("acc_dtype", StringType(), True),
    StructField("grs80tm_x", FloatType(), True),
    StructField("grs80tm_y", FloatType(), True),
])


# Kafka 스트리밍 데이터 읽기
df_stream = spark.readStream.format("kafka") \
    .option("kafka.bootstrap.servers", "kafka:9092") \
    .option("subscribe", "outbreak_topic") \
    .option("startingOffsets", "earliest") \
    .load()

# JSON 파싱 + timestamp 변환
df_json = df_stream.selectExpr("CAST(value AS STRING) as json_str") \
    .select(from_json(col("json_str"), schema).alias("data")) \
    .select("data.*")


df_json = df_json.withColumn(
    "OCCR_DATE",
    to_timestamp(concat_ws(" ", col("occr_date"), col("occr_time")), "yyyyMMdd HHmm")
).withColumn(
    "EXP_CLR_DATE",
    to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmm")
)

#  배치 단위 처리 함수 정의 (MySQL에 저장)
def process_batch(batch_df, batch_id):
    print(f"--- 배치 {batch_id} ---", flush=True)
    batch_df.show(5, truncate=False)
    #display(batch_df.limit(10))  # 주피터 셀에서 상위 10개 확인

    # batch_df.write \
    #   .format("jdbc") \
    #   .option("url", "jdbc:mysql://mysql:3306/weatherflow") \
    #   .option("driver", "com.mysql.cj.jdbc.Driver") \
    #   .option("dbtable", "weather_data") \
    #   .option("user", "user") \
    #   .option("password", "userpass") \
    #   .mode("append") \
    #   .save()

# 스트리밍 실행
query = df_json.writeStream \
    .foreachBatch(process_batch) \
    .outputMode("append") \
    .option("checkpointLocation", "/tmp/weatherflow_checkpoint") \
    .start()

# 스트리밍 유지
query.awaitTermination()

