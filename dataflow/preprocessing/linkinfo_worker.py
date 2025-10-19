import requests
import mysql.connector
import json
import time
import xml.etree.ElementTree as ET
from utils.redis_utils import RedisClient
import os
# Redis 연결
redis_client = RedisClient(host="redis", port=6379, db=0)

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
        return {
            "link_id": row.findtext("link_id"),
            "prcs_spd": int(row.findtext("prcs_spd") or 0),
            "prcs_trv_time": int(row.findtext("prcs_trv_time") or 0)
        }
    except Exception as e:
        print(f"[ERROR] TrafficInfo XML 파싱 실패: {e}")
        return None


# ------------------------------------------------------------------
# API 호출 함수
# ------------------------------------------------------------------
def fetch_linkinfo(link_id):
    url = f"http://openapi.seoul.go.kr:8088/{LINK_ID}/xml/LinkInfo/1/5/{link_id}/"
    try:
        res = requests.get(url, timeout=5)
        res.raise_for_status()
        return parse_linkinfo(res.text)
    except Exception as e:
        print(f"[ERROR] LinkInfo 호출 실패 ({link_id}): {e}")
        return None


def fetch_trafficinfo(link_id):
    url = f"http://openapi.seoul.go.kr:8088/{SEOUL_TRAFFIC_REALTIME_API_KEY}/xml/TrafficInfo/1/5/{link_id}/"
    try:
        res = requests.get(url, timeout=5)
        res.raise_for_status()
        return parse_trafficinfo(res.text)
    except Exception as e:
        print(f"[ERROR] TrafficInfo 호출 실패 ({link_id}): {e}")
        return None


# ------------------------------------------------------------------
# Redis → MySQL 저장 함수
# ------------------------------------------------------------------
def save_link_and_traffic_to_mysql():
    print("[SYSTEM] Link Worker 시작")
    while True:
        batch = []
        for _ in range(DB_BATCH_SIZE):
            item_json = redis_client.lpop("link_queue")
            if item_json is None:
                break
            try:
                item = json.loads(item_json)
                print(item)
            except json.JSONDecodeError:
                print(f"[ERROR] JSON 디코딩 실패: {item_json}")
                continue

            if "link_id" not in item or not item["link_id"]:
                print(f"[SKIP] link_id 없음: {item}")
                continue

            batch.append(item)

        if not batch:
            print("[INFO] link_queue에 처리할 데이터 없음, 5초 대기")
            time.sleep(5)
            continue

        try:
            conn = mysql.connector.connect(**MYSQL_CONFIG)
            cursor = conn.cursor()
        except mysql.connector.Error as e:
            print(f"[ERROR] MySQL 연결 실패: {e}")
            time.sleep(5)
            continue

        for entry in batch:
            link_id = entry.get("link_id")
            if not link_id:
                continue

            link_info = fetch_linkinfo(link_id)
            traffic_info = fetch_trafficinfo(link_id)
            print(link_info)
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

            if traffic_info:
                cursor.execute("""
                    INSERT INTO ROAD_TRAFFIC (LINK_ID, PRCS_SPD, PRCS_TRV_TIME)
                    VALUES (%(link_id)s, %(prcs_spd)s, %(prcs_trv_time)s)
                    ON DUPLICATE KEY UPDATE
                        PRCS_SPD = VALUES(PRCS_SPD),
                        PRCS_TRV_TIME = VALUES(PRCS_TRV_TIME)
                """, traffic_info)

        conn.commit()
        cursor.close()
        conn.close()
        print(f"[INFO] {len(batch)}개의 LINK_ID 처리 완료")


if __name__ == "__main__":
    save_link_and_traffic_to_mysql()
