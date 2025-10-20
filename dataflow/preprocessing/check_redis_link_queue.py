import redis
import json

# Redis 연결
r = redis.Redis(host="redis", port=6379, db=0, decode_responses=True)

# Redis ping 확인
try:
    pong = r.ping()
    print(f"[INFO] Redis 연결 확인: PING -> {pong}")
except Exception as e:
    print(f"[ERROR] Redis 연결 실패: {e}")
    exit(1)

# link_queue 데이터 확인
items = r.lrange("link_queue", 0, -1)
if not items:
    print("[INFO] link_queue에 데이터 없음")
else:
    print(f"[INFO] link_queue에 {len(items)}개 데이터 있음")
    for idx, item in enumerate(items, 1):
        try:
            data = json.loads(item)
            print(f"{idx}: {data}")
        except json.JSONDecodeError:
            print(f"{idx}: JSON 디코딩 실패 -> {item}")
