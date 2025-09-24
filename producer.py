from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, col, concat_ws, to_timestamp
from pyspark.sql.types import StructType, StructField, StringType, FloatType

# SparkSession 생성 (Kafka 패키지 포함)
spark = SparkSession.builder \
    .appName("WeatherFlowConsumerToMySQL") \
    .config(
        "spark.jars.packages",
        "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.5,"
        "mysql:mysql-connector-java:8.0.33"
    ) \
    .getOrCreate()

# Kafka 데이터 스키마 정의
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

# Kafka 스트리밍 읽기
df_stream = spark.readStream.format("kafka") \
    .option("kafka.bootstrap.servers", "kafka:9092") \
    .option("subscribe", "outbreak_topic") \
    .option("startingOffsets", "earliest") \
    .load()
print(111111111111111111111111111111111)
print(df_stream.selectExpr("CAST(value AS STRING)").show(5, truncate=False))


# JSON 파싱
df_json = df_stream.selectExpr("CAST(value AS STRING) as json_str") \
    .select(from_json(col("json_str"), schema).alias("data")) \
    .select("data.*") \
    .withColumn("grs80tm_x", col("grs80tm_x").cast("float")) \
    .withColumn("grs80tm_y", col("grs80tm_y").cast("float")) \
    .withColumn(
        "occr_datetime",
        to_timestamp(concat_ws(" ", col("occr_date"), col("occr_time")), "yyyyMMdd HHmm")
    ) \
    .withColumn(
        "exp_clr_datetime",
        to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmm")
    )


# 원래 문자열 컬럼 제거 (선택사항)
df_json = df_json.drop("occr_date", "occr_time", "exp_clr_date", "exp_clr_time")

# 배치 단위 처리 함수
def process_batch(batch_df, batch_id):
    print(f"--- 배치 {batch_id} ---", flush=True)
    batch_df.show(5, truncate=False)
    

    # MySQL 저장 예시
    # batch_df.write \
    #     .format("jdbc") \
    #     .option("url", "jdbc:mysql://mysql:3306/weatherflow") \
    #     .option("driver", "com.mysql.cj.jdbc.Driver") \
    #     .option("dbtable", "acc_info") \
    #     .option("user", "user") \
    #     .option("password", "userpass") \
    #     .mode("append") \
    #     .save()

# 스트리밍 실행
query = df_json.writeStream \
    .foreachBatch(process_batch) \
    .outputMode("append") \
    .option("checkpointLocation", "/tmp/accinfo_checkpoint") \
    .start()

query.awaitTermination()
