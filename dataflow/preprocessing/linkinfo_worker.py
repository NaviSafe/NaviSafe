import json, time, requests
from utils.redis_utils import RedisClient

redis_client = RedisClient(host="redis", port=6379, db=0)

def fetch_linkinfo(link_id):
    url = f"http://openapi.seoul.go.kr:8088/sample/xml/LinkInfo/1/5/{link_id}/"
    try:
        res = requests.get(url, timeout=5)
        res.raise_for_status()
        print(f"[INFO] LinkInfo API 호출 성공: {link_id}")
        return res.text  # 필요시 XML 파싱 가능
    except Exception as e:
        print(f"[ERROR] LinkInfo 호출 실패 ({link_id}): {e}")
        return None

def process_link_queue():
    while True:
        item = redis_client.lpop("link_queue")
        if not item:
            print("[INFO] link_queue 비어 있음. 5초 대기...")
            time.sleep(5)
            continue

        link_id = json.loads(item)["link_id"]
        data = fetch_linkinfo(link_id)
        if data:
            # 성공 시 후속 처리 (MySQL 저장 or Redis publish 등)
            pass
        else:
            # 실패 시 재시도 큐에 푸시
            redis_client.rpush_list("link_queue_retry", {"link_id": link_id})
            print(f"[WARN] LinkInfo 재시도 큐에 추가됨: {link_id}")

if __name__ == "__main__":
    print("[START] LinkInfo Worker 실행 시작")
    process_link_queue()
