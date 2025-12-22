import requests
import mysql.connector
import xml.etree.ElementTree as ET
import os
## airflow를 사용해서 일정 주기만다 api 호출하기.
from airflow.providers.mysql.hooks.mysql import MySqlHook

OUTBREAK_CODE_NAME = os.getenv('OUTBREAK_CODE_NAME')
# XML 파싱 함수
def parse_outbreak_code_info(xml_str):
    try:
        root = ET.fromstring(xml_str)
        rows = root.findall("row")
        result = []
        for r in rows:
            result.append({
                "acc_type": r.findtext("acc_type"),
                "acc_type_nm": r.findtext("acc_type_nm")
            })
        return result
    except Exception as e:
        print(f"[ERROR] AccMainCode XML 파싱 실패: {e}")
        return []

# API 호출
def fetch_outbreak_code_info():
    url = f"http://openapi.seoul.go.kr:8088/{OUTBREAK_CODE_NAME}/xml/AccMainCode/1/500/"
    res = requests.get(url, timeout=10)
    res.raise_for_status()
    return parse_outbreak_code_info(res.text)

# MySQL 저장
def save_outbreak_code_info_to_mysql(outbreak_code_list):
    mysql_hook = MySqlHook(mysql_conn_id="navisafe_mysql")
    conn = mysql_hook.get_conn()
    cursor = conn.cursor()

    sql="""
                INSERT INTO OUTBREAK_NAME (ACC_TYPE, ACC_TYPE_NM)
                VALUES (%(acc_type)s, %(acc_type_nm)s)
                ON DUPLICATE KEY UPDATE
                    ACC_TYPE_NM = VALUES(ACC_TYPE_NM)
            """

    cursor.executemany(sql, outbreak_code_list)
    conn.commit()
    cursor.close()
    conn.close()

    print(f"[INFO] {len(outbreak_code_list)}개의 돌발유형 코드 저장 완료")

# Airflow DAG에서 실행할 함수
def update_outbreak_code_name():
    print("[INFO] update_outbreak_code_name 실행 시작")
    try:
        outbreak_code = fetch_outbreak_code_info()
        if not outbreak_code:
            print("[WARN] 가져온 데이터가 없습니다.")
            return
        save_outbreak_code_info_to_mysql(outbreak_code)
        print("[INFO] update_outbreak_code_name 완료")
    except Exception as e:
        print(f"[ERROR] update_outbreak_code_name 실패: {e}")
        raise e
