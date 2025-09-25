from pyspark.sql import SparkSession
from pyspark.sql.functions import from_json, col, explode, to_timestamp, when, concat_ws, length
from pyspark.sql.types import StructType, StructField, StringType, ArrayType

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
# schema = StructType([
#     StructField("acc_id", StringType(), True),
#     StructField("link_id", StringType(), True),
#     StructField("occr_date", StringType(), True),
#     StructField("occr_time", StringType(), True),
#     StructField("exp_clr_date", StringType(), True),
#     StructField("exp_clr_time", StringType(), True),
#     StructField("acc_type", StringType(), True),
#     StructField("acc_dtype", StringType(), True),
#     StructField("acc_info", StringType(), True),
#     StructField("grs80tm_x", StringType(), True),
#     StructField("grs80tm_y", StringType(), True),
#     StructField("acc_road_code", StringType(), True),  # 새로 추가
# ])

    
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
    to_timestamp(concat_ws(" ", col("occr_date"), col("occr_time")), "yyyyMMdd HHmm")
).withColumn(
    "exp_clr_date_time",
    when(length(col("exp_clr_time")) == 4,
         to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmm"))
    .otherwise(to_timestamp(concat_ws(" ", col("exp_clr_date"), col("exp_clr_time")), "yyyyMMdd HHmmss"))
)

# 기존 컬럼 삭제
df_json = df_json.drop("occr_date", "occr_time", "exp_clr_date", "exp_clr_time")

#df_json.select("OCCR_DATE", "EXP_CLR_DATE").show(5, truncate=False)

#  배치 단위 처리 함수 정의 (MySQL에 저장)EXP_CLR_DATE
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

