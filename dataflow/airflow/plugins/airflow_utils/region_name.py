import requests
import mysql.connector
import xml.etree.ElementTree as ET
import os
## airflow를 사용해서 일정 주기만다 api 호출하기.
from airflow.providers.mysql.hooks.mysql import MySqlHook

REG_CODE = os.getenv('REG_CODE')
# XML 파싱 함수
def parse_regioninfo(xml_str):
    try:
        root = ET.fromstring(xml_str)
        rows = root.findall("row")
        result = []
        for r in rows:
            result.append({
                "reg_cd": r.findtext("reg_cd"),
                "reg_name": r.findtext("reg_name")
            })
        return result
    except Exception as e:
        print(f"[ERROR] RegionInfo XML 파싱 실패: {e}")
        return []


# API 호출
def fetch_regioninfo():
    url = f"http://openapi.seoul.go.kr:8088/{REG_CODE}/xml/RegionInfo/1/100/"
    res = requests.get(url, timeout=10)
    res.raise_for_status()
    return parse_regioninfo(res.text)

# MySQL 저장
def save_regioninfo_to_mysql(region_list):
    mysql_hook = MySqlHook(mysql_conn_id="navisafe_mysql")
    conn = mysql_hook.get_conn()
    cursor = conn.cursor()
    try:
        sql="""
                INSERT INTO REG_CD (REG_CD, REG_NAME)
                VALUES (%(reg_cd)s, %(reg_name)s)
                ON DUPLICATE KEY UPDATE
                    REG_NAME = VALUES(REG_NAME)
            """
        cursor.executemany(sql, region_list)
        conn.commit()
        print(f"[INFO] {len(region_list)}개의 지역 코드 저장 완료")
    except Exception as e:
        print(f"[ERROR] MySQL 저장 실패: {e}")
        raise
    finally:

        cursor.close()
        conn.close()

# Airflow에서 호출할 함수
def update_region_code_name():
    print("[INFO] update_region_code_name 실행 시작")
    try:
        regions = fetch_regioninfo()
        if not regions:
            print("[WARN] 가져온 데이터가 없습니다.")
            return
        save_regioninfo_to_mysql(regions)
        print("[INFO] update_region_code_name 완료")
    except Exception as e:
        print(f"[ERROR] update_region_code_name 실패: {e}")
        raise e
