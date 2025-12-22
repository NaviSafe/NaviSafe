import requests
import mysql.connector
import xml.etree.ElementTree as ET
import os
## airflow를 사용해서 일정 주기만다 api 호출하기.
from airflow.providers.mysql.hooks.mysql import MySqlHook

OUTBREAK_Detail_CODE = os.getenv('OUTBREAK_Detail_CODE')

# XML 파싱 함수
def parse_detail_code_info(xml_str):
    try:
        root = ET.fromstring(xml_str)
        rows = root.findall("row")
        result = []
        for r in rows:
            result.append({
                "acc_dtype": r.findtext("acc_dtype"),
                "acc_dtype_nm": r.findtext("acc_dtype_nm")
            })
        return result
    except Exception as e:
        print(f"[ERROR] AccSubCode XML 파싱 실패: {e}")
        return []

# API 호출
def fetch_detail_code_info():
    url = f"http://openapi.seoul.go.kr:8088/{OUTBREAK_Detail_CODE}/xml/AccSubCode/1/500/"
    res = requests.get(url, timeout=10)
    res.raise_for_status()
    return parse_detail_code_info(res.text)

# MySQL 저장
def save_detail_code_info_to_mysql(detail_code):
    mysql_hook = MySqlHook(mysql_conn_id="navisafe_mysql")
    conn = mysql_hook.get_conn()
    cursor = conn.cursor()
    sql ="""
            INSERT INTO OUTBREAK_DETAIL_CODE_NAME (ACC_DTYPE, ACC_DTYPE_NM)
            VALUES (%(acc_dtype)s, %(acc_dtype_nm)s)
            ON DUPLICATE KEY UPDATE
                ACC_DTYPE_NM = VALUES(ACC_DTYPE_NM)
        """
    cursor.executemany(sql, detail_code)
    conn.commit()
    cursor.close()
    conn.close()

    print(f"[INFO] {len(detail_code)}개의 돌발 세부 유형 코드 저장 완료")

# Airflow에서 호출할 함수
def update_outbreak_detail_code_name():
    print("[INFO] update_outbreak_detail_code_name 실행 시작")
    try:
        detail_code_info = fetch_detail_code_info()
        if not detail_code_info:
            print("[WARN] 가져온 데이터가 없습니다.")
            return
        save_detail_code_info_to_mysql(detail_code_info)
        print("[INFO] update_outbreak_detail_code_name 완료")
    except Exception as e:
        print(f"[ERROR] update_outbreak_detail_code_name 실패: {e}")
        raise e
