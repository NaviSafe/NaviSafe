import time
import os
import json
import requests
import xml.etree.ElementTree as ET
from kafka import KafkaProducer
from dotenv import load_dotenv

'''지하철호선ID(1001:1호선, 1002:2호선, 1003:3호선, 1004:4호선, 
1005:5호선 1006:6호선, 1007:7호선, 1008:8호선, 1009:9호선, 1063:경의중앙선,
1065:공항철도, 1067:경춘선, 1075:수인분당선 1077:신분당선, 1092:우이신설선, 1032:GTX-A)'''

load_dotenv()
OUTBREAK_KEY = os.getenv("OUTBREAK_KEY")    # 돌발정보
OUTBREAK_CODE_NAME = os.getenv("OUTBREAK_CODE_NAME")    # outbreak code name
OUTBREAK_Detail_CODE = os.getenv("OUTBREAK_Detail_CODE")    # outbreak detail code

REG_CODE = os.getenv("REG_CODE") # 권역코드

TRAFFIC_INFORMATION = os.getenv("TRAFFIC_INFORMATION") # 교통량
RAIN_API_KEY = os.getenv("RAIN_API_KEY")    # 강우량

## AccInfo, TrafficInfo 에 필요함 -> AccInfo, TrafficInfo에서 LINK_ID를 받아서 해당 api를 호출하여 정보를 반환 해야함
LINK_ID = os.getenv("LINK_ID")

### 지하철 현재 위치는 모든 호선을 가져옴
SEOUL_SUBWAY_POSITION_API_KEY = os.getenv("SEOUL_SUBWAY_POSITION_API_KEY") # 지하철 호선 필요 http://swopenAPI.seoul.go.kr/api/subway/(인증키)/xml/realtimePosition/(시작 페이지)/(종료페이지)/(호선) -> ex) 1호선

### 서울시 실시간 도로 소통 정보
SEOUL_TRAFFIC_REALTIME_API_KEY = os.getenv("SEOUL_TRAFFIC_REALTIME_API_KEY") # 링크 아이디 필요 http://openapi.seoul.go.kr:8088/(인증키)/xml/TrafficInfo/(시작 페이지)/(종료페이지)/(링크 아이디) -> ex) 1220003800



if __name__ == "__main__":
    api_list = [
        {'name': 'AccInfo', 'key': OUTBREAK_KEY, 'response_type' : 'xml'},
        {'name': 'AccMainCode', 'key': OUTBREAK_CODE_NAME, 'response_type' : 'xml'},
        {'name': 'AccSubCode', 'key': OUTBREAK_Detail_CODE, 'response_type' : 'xml'},
        {'name': 'LinkInfo', 'key': LINK_ID, 'response_type' : 'xml'},
        {'name': 'RegionInfo', 'key': REG_CODE, 'response_type' : 'xml'},
        {'name': 'TrafficInfo', 'key': SEOUL_TRAFFIC_REALTIME_API_KEY, 'response_type' : 'xml'},
        {'name': 'realtimePosition', 'key': SEOUL_SUBWAY_POSITION_API_KEY, 'response_type' : 'xml'}
    ]

lines = ['1호선', '2호선', '3호선', '4호선', '5호선', '6호선', '7호선', '8호선', '9호선', '신분당선', '경의중앙선', '공항철도']

# -----------------------------
# 토픽 매핑
# -----------------------------
topic_mapping = {
    'AccInfo': 'outbreak_topic',
    'AccMainCode' : 'outbreak_topic',
    'AccSubCode' : 'outbreak_topic',
    
    # 'LinkInfo' : 'realtime_trafficInfo',
    # 'TrafficInfo': 'realtime_trafficInfo',
    'RegionInfo' : 'realtime_trafficInfo',

    'ListRainfallService': 'rain_topic',
    'realtimeStationArrival': 'subway_arrival_topic',
    'realtimePosition': 'subway_position_topic'

    #'TrafficInfoRealtime': 'realtime_traffic_topic',
    
}
# http://swopenapi.seoul.go.kr/7a6b676a67646e64313035504a516556/xml/realtimePosition/1/500/1호선
# http://swopenapi.seoul.go.kr/api/subway/sample/xml/realtimePosition/0/5/1%ED%98%B8%EC%84%A0

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

# -----------------------------
# Kafka 연결
# -----------------------------
while True:
    try:
        print("Kafka 연결 시도 중...", flush=True)
        producer = KafkaProducer(
            bootstrap_servers='kafka:9092',
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
        print("Kafka 연결 성공!", flush=True)
        break
    except Exception as e:
        print("Kafka 연결 실패, 5초 후 재시도:", e, flush=True)
        time.sleep(5)

# -----------------------------
# 안전 호출 설정
# -----------------------------
LINES_PER_LOOP = 4          # 한 루프에서 호출할 호선 수
SLEEP_BETWEEN_LOOPS = 60    # 루프 간 대기 시간 (초)
line_index = 0

# -----------------------------
# 데이터 전송 함수
# -----------------------------
def send_to_kafka(api):
    api_name = api['name']
    response_type = api['response_type']
    topic = topic_mapping[api_name]

    try:
        if api_name == 'realtimePosition':
            # 호선별 안전 호출
            global line_index
            for _ in range(LINES_PER_LOOP):
                line = lines[line_index % len(lines)]
                url = f"http://swopenAPI.seoul.go.kr/api/subway/{api['key']}/{response_type}/{api_name}/{1}/{500}/{line}"
                #http://swopenAPI.seoul.go.kr/api/subway/7a6b676a67646e64313035504a516556/xml/realtimePosition/0/5/1호선
                response = requests.get(url, timeout=10)
                response.raise_for_status()
                data_dict = parse_xml_to_dict(response.text)
                producer.send(topic, {"line": line, "data": data_dict})
                producer.flush()
                print(f"{api_name} ({line}) → Kafka 전송 완료, 샘플:", data_dict[:2], flush=True)
                line_index += 1

        else:
            # 나머지 API
            url = f"http://openapi.seoul.go.kr:8088/{api['key']}/{response_type}/{api_name}/1/500/"
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            if response_type == 'xml':
                data_dict = parse_xml_to_dict(response.text)

            else:
                data_dict = response.json()
            producer.send(topic, data_dict)
            producer.flush()
            print(f"{api_name} 데이터 → 토픽 '{topic}' 전송 완료!", flush=True)
            print("전송 데이터 샘플:", data_dict[:2], flush=True)

    except Exception as e:
        print(f"{api_name} 데이터 전송 실패:", e, flush=True)

# -----------------------------
# 주기적 실행
# -----------------------------
while True:
    for api in api_list:
        send_to_kafka(api)
    time.sleep(5)  # 5초 주기