import time
import os
import json
import requests
import xml.etree.ElementTree as ET
from kafka import KafkaProducer
from dotenv import load_dotenv
import logging

'''
지하철호선ID(1001:1호선, 1002:2호선, 1003:3호선, 1004:4호선,
1005:5호선 1006:6호선, 1007:7호선, 1008:8호선, 1009:9호선,
1063:경의중앙선, 1065:공항철도, 1067:경춘선, 1075:수인분당선
1077:신분당선, 1092:우이신설선, 1032:GTX-A)
'''

load_dotenv()

# -----------------------------
# 환경 변수 로드
# -----------------------------
OUTBREAK_KEY = os.getenv("OUTBREAK_KEY")
REG_CODE = os.getenv("REG_CODE")
SEOUL_SUBWAY_POSITION_API_KEY = os.getenv("SEOUL_SUBWAY_POSITION_API_KEY")

# -----------------------------
# API 목록 정의
# -----------------------------
api_list = [
    {'name': 'AccInfo', 'key': OUTBREAK_KEY, 'response_type': 'xml'},
    {'name': 'RegionInfo', 'key': REG_CODE, 'response_type': 'xml'},
    {'name': 'realtimePosition', 'key': SEOUL_SUBWAY_POSITION_API_KEY, 'response_type': 'xml'},
]

lines = [
    '1호선', '2호선', '3호선', '4호선', '5호선',
    '6호선', '7호선', '8호선', '9호선',
    '신분당선', '경의중앙선', '공항철도'
]

# -----------------------------
# Kafka 토픽 매핑
# -----------------------------
topic_mapping = {
    'AccInfo': 'outbreak_topic',
    'RegionInfo': 'realtime_trafficInfo',
    'realtimePosition': 'subway_position_topic'
}

# -----------------------------
# XML → dict 변환 함수
# -----------------------------
def parse_xml_to_dict(xml_str):
    root = ET.fromstring(xml_str)
    result = []
    for row in root.findall('.//row'):
        row_dict = {}
        for elem in row:
            row_dict[elem.tag] = elem.text
        result.append(row_dict)
    return result


# =======================================================
# Airflow에서 호출 가능한 함수로 수정됨 (핵심 변경)
# =======================================================
def run_kafka_producer():
    """공공데이터 API → Kafka 토픽으로 전송 (Airflow PythonOperator에서 호출)"""
    log = logging.getLogger("airflow.task")

    # -----------------------------
    # Kafka 연결
    # -----------------------------
    try:
        log.info("[SYSTEM] Kafka 연결 시도 중...")
        producer = KafkaProducer(
            bootstrap_servers='kafka:9092',
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
        log.info("[SYSTEM] Kafka 연결 성공!")
    except Exception as e:
        log.error(f"[ERROR] Kafka 연결 실패: {e}")
        return

    # -----------------------------
    # 공공데이터 API → Kafka 전송
    # -----------------------------
    line_index = 0
    LINES_PER_LOOP = 4  # 한 번 실행 시 4개 노선만 호출

    for api in api_list:
        api_name = api['name']
        topic = topic_mapping[api_name]
        response_type = api['response_type']

        try:
            #지하철 실시간 위치 API
            if api_name == 'realtimePosition':
                for _ in range(LINES_PER_LOOP):
                    line = lines[line_index % len(lines)]
                    url = (
                        f"http://swopenAPI.seoul.go.kr/api/subway/"
                        f"{api['key']}/{response_type}/{api_name}/1/500/{line}"
                    )
                    response = requests.get(url, timeout=10)
                    response.raise_for_status()
                    data_dict = parse_xml_to_dict(response.text)

                    producer.send(topic, {"line": line, "data": data_dict})
                    producer.flush()
                    log.info(f"[Kafka] {api_name}({line}) → {topic} 전송 완료")

                    line_index += 1

            # 일반 공공데이터 API (AccInfo, RegionInfo)
            else:
                url = f"http://openapi.seoul.go.kr:8088/{api['key']}/{response_type}/{api_name}/1/500/"
                response = requests.get(url, timeout=10)
                response.raise_for_status()

                if response_type == 'xml':
                    data_dict = parse_xml_to_dict(response.text)
                else:
                    data_dict = response.json()

                producer.send(topic, data_dict)
                producer.flush()
                log.info(f"[Kafka] {api_name} 데이터 → {topic} 전송 완료")

        except Exception as e:
            log.error(f"[ERROR] {api_name} 전송 실패: {e}")

    # -----------------------------
    # 종료 처리
    # -----------------------------
    log.info("[SYSTEM] Kafka Producer 작업 완료 후 종료.")
    producer.close()