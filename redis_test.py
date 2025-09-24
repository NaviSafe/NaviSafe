import redis

# Redis 연결 (host='redis' → Compose 서비스 이름)
r = redis.Redis(host='redis', port=6379, db=0)

# String 저장
r.set('link_1220003800_speed', 15)
print("속도 저장 완료!")

# String 조회
speed = r.get('link_1220003800_speed')
print(f"조회된 속도: {int(speed)} km/h")

# Hash 저장
r.hset('event:1', mapping={'type': 'rain', 'severity': 'high', 'location': '강변북로'})
print("이벤트 저장 완료!")

# Hash 조회
event_type = r.hget('event:1', 'type')
print(f"조회된 이벤트 타입: {event_type.decode('utf-8')}")
