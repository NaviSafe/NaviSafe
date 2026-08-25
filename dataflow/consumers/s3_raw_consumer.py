import json
import logging
import os
import time
import uuid
from datetime import datetime, timezone

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from kafka import KafkaConsumer
from kafka.errors import KafkaError


# -------------------------------------------------------
# 로깅 설정
# -------------------------------------------------------
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(message)s",
)

log = logging.getLogger("s3-raw-consumer")


# -------------------------------------------------------
# Kafka 토픽별 S3 저장 경로
# -------------------------------------------------------
TOPIC_PATH_MAPPING = {
    "outbreak_topic": "raw/outbreak",
    "emergency_alert_topic": "raw/emergency_alert",
}


# -------------------------------------------------------
# Flush 기준
# -------------------------------------------------------
# 10분
FLUSH_INTERVAL_SECONDS = 600

# 메시지가 너무 많이 쌓이는 상황 대비
MAX_BATCH_SIZE = 1000


# -------------------------------------------------------
# Kafka Consumer 생성
# -------------------------------------------------------
def create_kafka_consumer() -> KafkaConsumer:
    kafka_bootstrap = os.getenv(
        "KAFKA_BOOTSTRAP",
        "kafka-svc.default:9092",
    )

    return KafkaConsumer(
        *TOPIC_PATH_MAPPING.keys(),
        bootstrap_servers=kafka_bootstrap,
        group_id="raw-s3-consumer",
        enable_auto_commit=False,
        auto_offset_reset="earliest",
        value_deserializer=lambda value: json.loads(
            value.decode("utf-8")
        ),
    )


# -------------------------------------------------------
# S3 Client 생성
# -------------------------------------------------------
def create_s3_client():
    region = os.getenv("AWS_REGION")

    if not region:
        raise ValueError("AWS_REGION 환경변수가 없습니다.")

    return boto3.client(
        "s3",
        region_name=region,
    )


# -------------------------------------------------------
# S3 Key 생성
# -------------------------------------------------------
def create_s3_key(topic: str) -> str:
    topic_path = TOPIC_PATH_MAPPING.get(topic)

    if not topic_path:
        raise ValueError(
            f"지원하지 않는 Kafka 토픽입니다: {topic}"
        )

    now = datetime.now(timezone.utc)

    return (
        f"{topic_path}/"
        f"date={now:%Y-%m-%d}/"
        f"batch-{now:%H%M%S}-{uuid.uuid4().hex}.jsonl"
    )


# -------------------------------------------------------
# 메시지 묶음을 JSONL로 S3 저장
# -------------------------------------------------------
def save_batch_to_s3(
    s3_client,
    bucket: str,
    topic: str,
    messages: list,
):
    if not messages:
        return

    key = create_s3_key(topic)

    # JSON Lines
    # 한 줄에 하나의 Kafka 메시지
    body = "\n".join(
        json.dumps(
            {
                "topic": message.topic,
                "partition": message.partition,
                "offset": message.offset,
                "timestamp": message.timestamp,
                "value": message.value,
            },
            ensure_ascii=False,
        )
        for message in messages
    )

    s3_client.put_object(
        Bucket=bucket,
        Key=key,
        Body=body.encode("utf-8"),
        ContentType="application/x-ndjson",
    )

    log.info(
        "S3 저장 완료 | topic=%s count=%s key=%s",
        topic,
        len(messages),
        key,
    )


# -------------------------------------------------------
# Buffer 전체 Flush
# -------------------------------------------------------
def flush_buffers(
    s3_client,
    bucket: str,
    buffers: dict,
):
    flushed = False

    for topic, messages in buffers.items():
        if not messages:
            continue

        save_batch_to_s3(
            s3_client=s3_client,
            bucket=bucket,
            topic=topic,
            messages=messages,
        )

        buffers[topic] = []
        flushed = True

    return flushed


# -------------------------------------------------------
# S3 Raw Consumer
# -------------------------------------------------------
def run_s3_raw_consumer():
    bucket = os.getenv("S3_BUCKET")

    if not bucket:
        raise ValueError("S3_BUCKET 환경변수가 없습니다.")

    consumer = None

    # 토픽별 Buffer
    buffers = {
        topic: []
        for topic in TOPIC_PATH_MAPPING
    }

    last_flush_time = time.time()

    try:
        consumer = create_kafka_consumer()
        s3_client = create_s3_client()

        log.info("S3 Raw Consumer 실행 시작")

        log.info(
            "구독 토픽: %s",
            ", ".join(TOPIC_PATH_MAPPING.keys()),
        )

        log.info(
            "S3 버킷: %s",
            bucket,
        )

        log.info(
            "Flush 주기: %s초",
            FLUSH_INTERVAL_SECONDS,
        )

        for message in consumer:
            topic = message.topic

            if topic not in buffers:
                log.warning(
                    "지원하지 않는 토픽 수신: %s",
                    topic,
                )
                continue

            # Kafka 메시지를 토픽별 Buffer에 저장
            buffers[topic].append(message)

            current_time = time.time()

            interval_reached = (
                current_time - last_flush_time
                >= FLUSH_INTERVAL_SECONDS
            )

            batch_size_reached = (
                len(buffers[topic])
                >= MAX_BATCH_SIZE
            )

            # 10분이 지났거나
            # 메시지가 1000건 이상 쌓이면 S3 저장
            if interval_reached or batch_size_reached:

                try:
                    flushed = flush_buffers(
                        s3_client=s3_client,
                        bucket=bucket,
                        buffers=buffers,
                    )

                    if flushed:
                        # 모든 S3 저장이 성공한 후에만 Commit
                        consumer.commit()

                        log.info(
                            "Kafka Offset Commit 완료"
                        )

                    last_flush_time = current_time

                except (
                    BotoCoreError,
                    ClientError,
                    ValueError,
                    TypeError,
                ) as error:

                    log.exception(
                        "S3 Batch 저장 실패: %s",
                        error,
                    )

                    # Commit하지 않고 종료
                    # Deployment가 재시작되면
                    # 마지막 Commit 이후 메시지를 다시 읽음
                    raise

    except KafkaError:
        log.exception(
            "Kafka Consumer 실행 중 오류 발생"
        )
        raise

    except Exception:
        log.exception(
            "S3 Raw Consumer 실행 실패"
        )
        raise

    finally:
        # 종료 전에 남은 메시지 저장 시도
        try:
            if consumer is not None:
                s3_client = create_s3_client()

                flushed = flush_buffers(
                    s3_client=s3_client,
                    bucket=bucket,
                    buffers=buffers,
                )

                if flushed:
                    consumer.commit()

                    log.info(
                        "종료 전 남은 Buffer 저장 완료"
                    )

        except Exception:
            log.exception(
                "종료 전 Buffer 저장 실패"
            )

        finally:
            if consumer is not None:
                consumer.close()

                log.info(
                    "Kafka Consumer 연결 종료"
                )


# -------------------------------------------------------
# 실행
# -------------------------------------------------------
if __name__ == "__main__":
    run_s3_raw_consumer()