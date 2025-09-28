FROM python:3.11-slim 
#bitnami/spark:3.5.5


WORKDIR /app

COPY producer.py .
COPY ./consumers ./consumers

COPY ./preprocessing /app/preprocessing
COPY .env .
COPY requirements.txt .
# RUN apt-get update && \
#     apt-get install -y python3 python3-pip python3-distutils curl procps && \
#     apt-get clean


RUN pip install --no-cache-dir -r requirements.txt
RUN pip install --no-cache-dir pyspark==3.5.5 pymysql sqlalchemy

# Java home 경로 확인 (슬림 이미지에서는 보통 여기 설치됨)
# ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
# ENV PATH=$JAVA_HOME/bin:$PATH


ENV SPARK_PACKAGES="org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.5,mysql:mysql-connector-java:8.0.33"

#docker run -it --rm openjdk:17-jdk bash
#ls /usr/lib/jvm