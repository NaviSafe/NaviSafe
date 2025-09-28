# spark_session.py
from pyspark.sql import SparkSession

def get_spark(app_name="SparkApp", kafka=True):
    """
    SparkSession 생성 및 공통 설정
    kafka=True: Kafka 패키지 포함
    """
    builder = SparkSession.builder.appName(app_name)
    
    if kafka:
        builder = builder.config(
            "spark.jars.packages",
            "org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.5,"  # Kafka
            "mysql:mysql-connector-java:8.0.33"                    # MySQL
        )
    builder = builder.config("spark.sql.legacy.timeParserPolicy", "LEGACY")
    
    spark = builder.getOrCreate()
    return spark
