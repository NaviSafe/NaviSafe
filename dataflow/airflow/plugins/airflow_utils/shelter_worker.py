import os
import time
import requests
import xml.etree.ElementTree as ET
import mysql.connector
from pyproj import Transformer

# ------------------------------------------------------------------
#  환경 변수(API KEY)
# ------------------------------------------------------------------
EARTHQUAKE_SHELTER_API_KEY = os.getenv('EARTHQUAKE_SHELTER_API_KEY')   # 서울시 지진대피소
EARTHQUAKE_OUTDOOR_API_KEY = os.getenv('EARTHQUAKE_OUTDOOR_API_KEY')   # 서울시 옥외 지진대피소
SUMMER_SHELTER_API_KEY = os.getenv('SUMMER_SHELTER_API_KEY')           # 무더위 쉼터
FINE_DUST_SHELTER_API_KEY = os.getenv('FINE_DUST_SHELTER_API_KEY')     # 미세먼지 쉼터

# ------------------------------------------------------------------
# MySQL 설정
# ------------------------------------------------------------------
MYSQL_CONFIG = {
    "host": "mysql",
    "user": "user",
    "password": "userpass",
    "database": "toy_project"
}

# ------------------------------------------------------------------
# 좌표 변환기 설정 (GRS80TM → WGS84)
# ------------------------------------------------------------------

transformer = Transformer.from_crs("EPSG:5186", "EPSG:4326", always_xy=True)


def convert_to_gps(x, y):
    try:
        lon, lat = transformer.transform(float(x), float(y))
        return lon, lat
    except Exception as e:
        print(f"[ERROR] 좌표 변환 실패: x={x}, y={y}, error={e}")
        return None, None


# ------------------------------------------------------------------
# 공통 API 호출 함수
# ------------------------------------------------------------------
def fetch_api(url):
    try:
        res = requests.get(url, timeout=10)
        res.raise_for_status()
        return res.text
    except Exception as e:
        print(f"[ERROR] API 호출 실패: {e}")
        return None


# ------------------------------------------------------------------
# XML 파싱 함수 (API별 구조 상이)
# ------------------------------------------------------------------
def parse_shelter_data(xml_str, shelter_code):
    shelters = []
    try:
        root = ET.fromstring(xml_str)
        for row in root.findall("row"):
            name, address, lot, lat = None, None, None, None

            # 서울시 지진대피소
            if shelter_code == 1:
                name = row.findtext("FCLT_NM")
                address = row.findtext("DADDR")
                lot = row.findtext("LOT")
                lat = row.findtext("LAT")

            # 서울시 옥외 지진대피소
            elif shelter_code == 2:
                name = row.findtext("ACTC_FCLT_NM")
                address = row.findtext("DADDR")
                lot = row.findtext("LOT")
                lat = row.findtext("LAT")
                #lot, lat = convert_to_gps(x, y)

            # 무더위 쉼터
            elif shelter_code == 3:
                name = row.findtext("R_AREA_NM")
                address = row.findtext("LOTNO_ADDR")
                lot = row.findtext("LON")
                lat = row.findtext("LAT")

            # 미세먼지 쉼터
            elif shelter_code == 4:
                name = row.findtext("FCLT_NM")
                address = row.findtext("ADDR")
                x = row.findtext("XCRD")
                y = row.findtext("YCRD")
                lot, lat = convert_to_gps(x, y)

            # 필수 값 확인
            if not (name and lot and lat):
                continue

            shelters.append({
                "SHELTER_CODE": shelter_code,
                "SHELTER_NAME": name.strip(),
                "SHELTER_ADDRESS": address or "",
                "LOT": float(lot),
                "LAT": float(lat)
            })

        return shelters
    except Exception as e:
        print(f"[ERROR] XML 파싱 실패 (코드 {shelter_code}): {e}")
        return []


# ------------------------------------------------------------------
# MySQL 저장 함수
# ------------------------------------------------------------------
def save_to_db(shelter_list):
    if not shelter_list:
        return

    conn = mysql.connector.connect(**MYSQL_CONFIG)
    cursor = conn.cursor()

    cursor.executemany("""
        INSERT INTO SHELTER_GPS (SHELTER_CODE, SHELTER_NAME, SHELTER_ADDRESS, LOT, LAT)
        VALUES (%(SHELTER_CODE)s, %(SHELTER_NAME)s, %(SHELTER_ADDRESS)s, %(LOT)s, %(LAT)s)
        ON DUPLICATE KEY UPDATE
            SHELTER_ADDRESS = VALUES(SHELTER_ADDRESS),
            LOT = VALUES(LOT),
            LAT = VALUES(LAT)
    """, shelter_list)

    conn.commit()
    cursor.close()
    conn.close()
    print(f"[INFO] {len(shelter_list)}개의 데이터 저장 완료")


# ------------------------------------------------------------------
# 전체 실행 함수
# ------------------------------------------------------------------
def run_shelter_worker():
    api_targets = [
        (1, f"http://openapi.seoul.go.kr:8088/{EARTHQUAKE_SHELTER_API_KEY}/xml/TbEqkShelter/1/1000/"),
        (2, f"http://openapi.seoul.go.kr:8088/{EARTHQUAKE_OUTDOOR_API_KEY}/xml/TlEtqkP/1/1000/"),
        (3, f"http://openapi.seoul.go.kr:8088/{SUMMER_SHELTER_API_KEY}/xml/TbGtnHwcwP/1/1000/"),
        (4, f"http://openapi.seoul.go.kr:8088/{FINE_DUST_SHELTER_API_KEY}/xml/shuntPlace/1/1000/")
    ]

    for shelter_code, url in api_targets:
        print(f"[INFO] API 호출 시작 → 코드 {shelter_code}")
        xml_data = fetch_api(url)
        if not xml_data:
            continue

        shelter_data = parse_shelter_data(xml_data, shelter_code)
        save_to_db(shelter_data)
        time.sleep(1)  # API rate limit 방지

    print("[SYSTEM] 모든 Shelter 데이터 수집 완료 ")


if __name__ == "__main__":
    run_shelter_worker()
