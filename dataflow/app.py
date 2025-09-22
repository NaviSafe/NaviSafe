from flask import Flask, request, jsonify
import requests
import xml.etree.ElementTree as ET
import os
from dotenv import load_dotenv

load_dotenv()
SEOUL_SUBWAY_ARRIVAL_API_KEY = os.getenv("SEOUL_SUBWAY_ARRIVAL_API_KEY")  # 지하철 도착 정보

SEOUL_TRANSIT_API_KEY = os.getenv("SEOUL_TRANSIT_API_KEY")  # 환승 정보

app = Flask(__name__)

# -----------------------------
# XML → dict 변환 함수 (item_tag 지정 가능)
# -----------------------------
def parse_xml_to_dict(xml_str, item_tag="row"):
    root = ET.fromstring(xml_str)
    result = []
    for row in root.findall(f".//{item_tag}"):
        row_dict = {elem.tag: elem.text for elem in row}
        result.append(row_dict)
    return result

# -----------------------------
# 도착 정보 조회
# -----------------------------
@app.route('/arrival', methods=['GET'])
def get_arrival():
    station_name = request.args.get('station')
    if not station_name:
        return jsonify({"error": "station query parameter required"}), 400

    url = f"http://swopenAPI.seoul.go.kr/api/subway/{SEOUL_SUBWAY_ARRIVAL_API_KEY}/xml/realtimeStationArrival/1/100/{station_name}"

    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data_dict = parse_xml_to_dict(response.text)
        return jsonify({"station": station_name, "data": data_dict})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# -----------------------------
# 정류장/역 이름 → WGS84 좌표
# -----------------------------
def get_coordinates(station_name):
    url = "http://ws.bus.go.kr/api/rest/pathinfo/getLocationInfo"
    params = {
        "ServiceKey": SEOUL_TRANSIT_API_KEY,
        "stSrch": station_name   # quote 불필요
    }
    
    response = requests.get(url, params=params, timeout=10)
    response.raise_for_status()
    data = parse_xml_to_dict(response.text, item_tag="itemList")  # itemList 사용
    if not data:
        raise ValueError(f"{station_name}에 대한 좌표를 찾을 수 없습니다.")
    # 첫 번째 결과 사용
    gpsX = data[0].get("gpsX")  # 경도
    gpsY = data[0].get("gpsY")  # 위도
    return gpsX, gpsY

# -----------------------------
# XML → dict 변환 함수 (일반 itemList + subPath 처리)
# -----------------------------
def parse_transfer_xml(xml_str):
    root = ET.fromstring(xml_str)
    result = []

    for item in root.findall('.//itemList'):
        item_dict = {
            "distance": item.findtext("distance"),
            "time": item.findtext("time"),
            "pathList": []
        }

        for path in item.findall("pathList"):
            path_dict = {
                "routeId": path.findtext("routeId"),
                "routeNm": path.findtext("routeNm"),
                "fid": path.findtext("fid"),
                "fname": path.findtext("fname"),
                "fx": path.findtext("fx"),
                "fy": path.findtext("fy"),
                "tid": path.findtext("tid"),
                "tname": path.findtext("tname"),
                "tx": path.findtext("tx"),
                "ty": path.findtext("ty")
            }
            item_dict["pathList"].append(path_dict)

        result.append(item_dict)

    return result


# -----------------------------
# 환승 경로 조회
# -----------------------------
## http://localhost:5000/transfer?start=신림&end=영등포&transport=bus
@app.route('/transfer', methods=['GET'])
def get_transfer_info():
    start_name = request.args.get('start')
    end_name = request.args.get('end')
    transport = request.args.get('transport', 'bus_subway')  # 기본값: 지하철+버스

    if not start_name or not end_name:
        return jsonify({"error": "start and end query parameters required"}), 400

    try:
        # 이름 → 좌표 변환
        stX, stY = get_coordinates(start_name)
        edX, edY = get_coordinates(end_name)

        # API URL 선택
        if transport == 'subway':
            url = "http://ws.bus.go.kr/api/rest/pathinfo/getPathInfoBySubway"
        elif transport == 'bus':
            url = "http://ws.bus.go.kr/api/rest/pathinfo/getPathInfoByBus"
        else: # 지하철 + 버스
            url = "http://ws.bus.go.kr/api/rest/pathinfo/getPathInfoByBusNSub"

        params = {
            "ServiceKey": SEOUL_TRANSIT_API_KEY,
            "startX": stX,
            "startY": stY,
            "endX": edX,
            "endY": edY
        }

        response = requests.get(url, params=params, timeout=10)
        response.raise_for_status() # 오류 발생시 예외 발생

        transfer_data = parse_transfer_xml(response.text)  # 공통 파서 사용

        return jsonify({
            "start": start_name,
            "end": end_name,
            "transport": transport,
            "transfer_info": transfer_data
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# -----------------------------
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
