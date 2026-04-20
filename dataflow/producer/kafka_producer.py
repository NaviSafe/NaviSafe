import time
import os
import json
import requests
import xml.etree.ElementTree as ET
from kafka import KafkaProducer
import logging
import math
from dotenv import load_dotenv

load_dotenv("/opt/airflow/.env")
# -----------------------------
# 환경 변수 로드
# -----------------------------
OUTBREAK_KEY = os.getenv("OUTBREAK_KEY")
REG_CODE = os.getenv("REG_CODE")
SEOUL_SUBWAY_POSITION_API_KEY = os.getenv("SEOUL_SUBWAY_POSITION_API_KEY")
EMERGENCY_ALERT_API_KEY = os.getenv("EMERGENCY_ALERT_API_KEY")


# -----------------------------
# API 목록 정의
# -----------------------------
api_list = [
    {"name": "AccInfo", "key": OUTBREAK_KEY, "response_type": "xml"},
    {"name": "RegionInfo", "key": REG_CODE, "response_type": "xml"},
    {"name": "emergencyAlert", "key": EMERGENCY_ALERT_API_KEY, "response_type": "json"},
]


# -----------------------------
# Kafka 토픽 매핑
# -----------------------------
topic_mapping = {
    "AccInfo": "outbreak_topic",
    "RegionInfo": "realtime_trafficInfo",
    "emergencyAlert": "emergency_alert_topic",
}


# -----------------------------
# XML → dict 변환
# -----------------------------
def parse_xml_to_dict(xml_str):
    root = ET.fromstring(xml_str)
    result = []
    for row in root.findall(".//row"):
        row_dict = {elem.tag: elem.text for elem in row}
        result.append(row_dict)
    return result


# =======================================================
# Kafka Producer 실행 함수
# =======================================================
def run_kafka_producer():
    # -----------------------------
    # logging 설정 (kubectl logs 출력 보장)
    # -----------------------------
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s | %(levelname)s | %(message)s",
    )
    log = logging.getLogger(__name__)
    log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!수정코드!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
    log.info("Producer 실행 시작")

    # -----------------------------
    # Kafka 연결
    # -----------------------------
    try:
        log.info("Kafka 연결 시도")
        producer = KafkaProducer(
            bootstrap_servers="kafka-svc.default:9092",
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        )
        log.info("Kafka 연결 성공")
    except Exception:
        log.exception("Kafka 연결 실패")
        raise

    # -----------------------------
    # API 호출 및 전송
    # -----------------------------
    for api in api_list:
        api_name = api["name"]
        api_key = api["key"]
        response_type = api["response_type"]
        topic = topic_mapping[api_name]

        if not api_key:
            log.error(f"{api_name} API KEY 없음 → 건너뜀")
            continue

        try:
            if api_name == "emergencyAlert":
                url = "https://www.safetydata.go.kr/V2/api/DSSP-IF-00247"

                meta_params = {
                    "serviceKey": api_key,
                    "returnType": "json",
                    "numOfRows": 1,
                    "pageNo": 1,
                }

                meta_resp = requests.get(url, params=meta_params, timeout=10)
                meta_resp.raise_for_status()
                total_count = meta_resp.json().get("totalCount", 0)

                if total_count == 0:
                    log.info("emergencyAlert totalCount=0 → 전송 없음")
                    continue

                num_of_rows = 100
                last_page = math.ceil(total_count / num_of_rows)

                params = {
                    "serviceKey": api_key,
                    "returnType": "json",
                    "numOfRows": num_of_rows,
                    "pageNo": last_page,
                }

                response = requests.get(url, params=params, timeout=10)
                response.raise_for_status()

                data_dict = response.json()
                body = data_dict.get("body")

                if not body:
                    log.info("emergencyAlert body 없음 → 전송 생략")
                    continue

                producer.send(topic, body)
                log.info(f"emergencyAlert {len(body)}건 → {topic} 전송")

            else:
                url = (
                    f"http://openapi.seoul.go.kr:8088/"
                    f"{api_key}/{response_type}/{api_name}/1/500/"
                )

                response = requests.get(url, timeout=10)
                response.raise_for_status()

                if response_type == "xml":
                    data_dict = parse_xml_to_dict(response.text)
                else:
                    data_dict = response.json()

                if not data_dict:
                    log.info(f"{api_name} 데이터 없음 → 전송 생략")
                    continue

                producer.send(topic, data_dict)
                log.info(f"{api_name} {len(data_dict)}건 → {topic} 전송")

        except Exception:
            log.exception(f"{api_name} 처리 중 오류 발생")

    # -----------------------------
    # 종료 처리
    # -----------------------------
    producer.flush()
    producer.close()
    log.info("Producer 작업 완료")


# -------------------------------------------------------
# 단독 실행용 (Airflow 외 환경)
# -------------------------------------------------------
# if __name__ == "__main__":
#     while True:
#         run_kafka_producer()
#         time.sleep(5)