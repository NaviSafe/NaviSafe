import requests
import mysql.connector
import xml.etree.ElementTree as ET
import os
## airflow를 사용해서 일정 주기만다 api 호출하기.

REG_CODE = os.getenv('REG_CODE')
MYSQL_CONFIG = {
    "host": "mysql",
    "user": "user",
    "password": "userpass",
    "database": "toy_project"
}

# XML 파싱 함수
def parse_regioninfo(xml_str):
    try:
        root = ET.fromstring(xml_str)
        rows = root.findall("row")
        return [{"reg_cd": int(r.findtext("reg_cd")), "reg_name": r.findtext("reg_name")} for r in rows]
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
    conn = mysql.connector.connect(**MYSQL_CONFIG)
    cursor = conn.cursor()
    for region in region_list:
        cursor.execute("""
            INSERT INTO REGION_CD (REG_CD, REG_NAME)
            VALUES (%(reg_cd)s, %(reg_name)s)
            ON DUPLICATE KEY UPDATE
                REG_NAME = VALUES(REG_NAME)
        """, region)
    conn.commit()
    cursor.close()
    conn.close()
    print(f"[INFO] {len(region_list)}개의 지역 코드 저장 완료")

if __name__ == "__main__":
    regions = fetch_regioninfo()
    save_regioninfo_to_mysql(regions)
