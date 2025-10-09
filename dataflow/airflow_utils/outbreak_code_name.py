import requests
import mysql.connector
import xml.etree.ElementTree as ET
import os
## airflow를 사용해서 일정 주기만다 api 호출하기.

OUTBREAK_CODE_NAME = os.getenv('OUTBREAK_CODE_NAME')
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
        return [{"acc_type": int(r.findtext("acc_type")), "acc_type_nm": r.findtext("acc_type_nm")} for r in rows]
    except Exception as e:
        print(f"[ERROR] AccMainCode XML 파싱 실패: {e}")
        return []

# API 호출
def fetch_regioninfo():
    url = f"http://openapi.seoul.go.kr:8088/{OUTBREAK_CODE_NAME}/xml/AccMainCode/1/500/"
    res = requests.get(url, timeout=10)
    res.raise_for_status()
    return parse_regioninfo(res.text)

# MySQL 저장
def save_regioninfo_to_mysql(region_list):
    conn = mysql.connector.connect(**MYSQL_CONFIG)
    cursor = conn.cursor()
    for region in region_list:
        cursor.execute("""
            INSERT INTO OUTBREAK_DETAIL_CODE_NAME (ACC_TYPE, ACC_TYPE_NM)
            VALUES (%(acc_type)s, %(acc_type_nm)s)
            ON DUPLICATE KEY UPDATE
                ACC_TYPE_NM = VALUES(ACC_TYPE_NM)
        """, region)
    conn.commit()
    cursor.close()
    conn.close()
    print(f"[INFO] {len(region_list)}개의 돌발유형 코드 저장 완료")

if __name__ == "__main__":
    regions = fetch_regioninfo()
    save_regioninfo_to_mysql(regions)
