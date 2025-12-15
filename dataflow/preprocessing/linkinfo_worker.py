import requests
import mysql.connector
import json
import time
import xml.etree.ElementTree as ET
import sys, os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from utils.redis_utils import RedisClient
import os
# Redis 연결
redis_client = RedisClient(host="redis", port=6379, db=0)

# Redis 연결

LINK_ID = os.getenv("LINK_ID") 
SEOUL_TRAFFIC_REALTIME_API_KEY = os.getenv("SEOUL_TRAFFIC_REALTIME_API_KEY") 
DB_BATCH_SIZE = 50
MYSQL_CONFIG = {
    "host": "mysql",
    "user": "user",
    "password": "userpass",
    "database": "toy_project"
}

# ------------------------------------------------------------------
# XML 파싱 함수
# ------------------------------------------------------------------
def parse_linkinfo(xml_str):
    try:
        root = ET.fromstring(xml_str)
        row = root.find("row")
        if row is None:
            return None
        return {
            "link_id": row.findtext("link_id"),
            "road_name": row.findtext("road_name"),
            "st_node_nm": row.findtext("st_node_nm"),
            "ed_node_nm": row.findtext("ed_node_nm"),
            "map_dist": int(row.findtext("map_dist") or 0),
            "reg_cd": int(row.findtext("reg_cd"))
        }
    except Exception as e:
        print(f"[ERROR] LinkInfo XML 파싱 실패: {e}")
        return None


def parse_trafficinfo(xml_str):
    try:
        root = ET.fromstring(xml_str)
        row = root.find("row")
        if row is None:
            return None
        prcs_spd_text = row.findtext("prcs_spd") or "0"
        prcs_trv_time_text = row.findtext("prcs_trv_time") or "0"
        return {
            "link_id": row.findtext("link_id"),
            "prcs_spd": float(prcs_spd_text),        # float로 변환
            "prcs_trv_time": int(float(prcs_trv_time_text))  # 필요 시 int
        }
    except Exception as e:
        print(f"[ERROR] TrafficInfo XML 파싱 실패: {e}")
        return None


# ------------------------------------------------------------------
# API 호출 함수
# ------------------------------------------------------------------
def fetch_linkinfo(link_id):
    global LINK_ID  # 전역 변수 사용 명시
    url = f"http://openapi.seoul.go.kr:8088/{LINK_ID}/xml/LinkInfo/1/5/{link_id}/"
    print(f"Calling LinkInfo API: {url}")
    try:
        res = requests.get(url, timeout=5)
        res.raise_for_status()
        return parse_linkinfo(res.text)
    except Exception as e:
        print(f"[ERROR] LinkInfo 호출 실패 ({link_id}): {e}")
        return None


def fetch_trafficinfo(link_id):
    global SEOUL_TRAFFIC_REALTIME_API_KEY  # 전역 변수 사용 명시
    url = f"http://openapi.seoul.go.kr:8088/{SEOUL_TRAFFIC_REALTIME_API_KEY}/xml/TrafficInfo/1/5/{link_id}/"
    print(f"Calling TrafficInfo API: {url}")
    try:
        res = requests.get(url, timeout=5)
        res.raise_for_status()
        return parse_trafficinfo(res.text)
    except Exception as e:
        print(f"[ERROR] TrafficInfo 호출 실패 ({link_id}): {e}")
        return None

def run_linkinfo_worker():
    """Redis link_queue 데이터를 MySQL로 옮기고 종료"""
    try:
        conn = mysql.connector.connect(
            host="mysql",
            user="user",
            password="userpass",
            database="toy_project"
        )
        cursor = conn.cursor(dictionary=True)

        batch = []
        # Redis 큐에 데이터가 존재할 때만 꺼내 처리
        while True:
            item = redis_client.lpop("link_queue")
            if not item:
                break  # 큐가 비면 종료
            print("Redis에서 꺼낸 item:", item)
            data = json.loads(item)
            link_id = data.get("link_id")
            print("파싱 후 link_id:", link_id)
            if link_id:
                batch.append(data)

        if not batch:
            print("[INFO] 처리할 link_queue 데이터가 없습니다.")
            return

        print(f"[INFO] {len(batch)}개의 LINK_ID 데이터를 처리합니다...")

        for entry in batch:
            link_id = entry.get("link_id")
            if not link_id:
                continue

            link_info = fetch_linkinfo(link_id)
            print(link_info)
            traffic_info = fetch_trafficinfo(link_id)

            if link_info:
                cursor.execute("""
                    INSERT INTO LINK_ID (LINK_ID, ROAD_NAME, ST_NODE_NM, ED_NODE_NM, MAP_DIST, REG_CD_REG_CD)
                    VALUES (%(link_id)s, %(road_name)s, %(st_node_nm)s, %(ed_node_nm)s, %(map_dist)s, %(reg_cd)s)
                    ON DUPLICATE KEY UPDATE
                        ROAD_NAME = VALUES(ROAD_NAME),
                        ST_NODE_NM = VALUES(ST_NODE_NM),
                        ED_NODE_NM = VALUES(ED_NODE_NM),
                        MAP_DIST = VALUES(MAP_DIST),
                        REG_CD_REG_CD = VALUES(REG_CD_REG_CD)
                """, link_info)
                print(f"link_info 데이터 삽입 완료")

            if traffic_info:
                cursor.execute("""
                    INSERT INTO ROAD_TRAFFIC (LINK_ID, PRCS_SPD, PRCS_TRV_TIME)
                    VALUES (%(link_id)s, %(prcs_spd)s, %(prcs_trv_time)s)
                    ON DUPLICATE KEY UPDATE
                        PRCS_SPD = VALUES(PRCS_SPD),
                        PRCS_TRV_TIME = VALUES(PRCS_TRV_TIME)
                """, traffic_info)
                print(f"ROAD_TRAFFIC 데이터 삽입 완료")

        conn.commit()
        print(f"[INFO] {len(batch)}개의 LINK_ID 데이터 처리 완료")

    except Exception as e:
        print(f"[ERROR] 데이터 저장 중 오류 발생: {e}")
    finally:
        cursor.close()
        conn.close()

if __name__ == "__main__":
    run_linkinfo_worker()
