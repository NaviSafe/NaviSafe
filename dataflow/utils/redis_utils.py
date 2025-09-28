import redis
import json

class RedisClient:
    def __init__(self, host="localhost", port=6379, db=0):
        """
        Redis 연결 초기화
        """
        self.r = redis.Redis(host=host, port=port, db=db, decode_responses=True)

    # -------------------------------
    # 단일 키-값 저장
    # -------------------------------
    def set_key(self, key, value, ex=None):
        """
        key: str
        value: dict 또는 str
        ex: TTL(초 단위, optional)
        """
        if isinstance(value, dict):
            value = json.dumps(value, ensure_ascii=False)
        self.r.set(key, value, ex=ex)

    # -------------------------------
    # 리스트에 push
    # -------------------------------
    def rpush_list(self, list_name, value):
        """
        리스트 끝에 데이터 추가
        value: dict 또는 str
        """
        if isinstance(value, dict):
            value = json.dumps(value, ensure_ascii=False)
        self.r.rpush(list_name, value)

    # -------------------------------
    # Pub/Sub 발행
    # -------------------------------
    def publish_channel(self, channel, message):
        """
        채널에 메시지 발행
        message: dict 또는 str
        """
        if isinstance(message, dict):
            message = json.dumps(message, ensure_ascii=False)
        self.r.publish(channel, message)

    # -------------------------------
    # 리스트 조회 / 제거
    # -------------------------------
    def lrange(self, list_name, start=0, end=-1):
        return self.r.lrange(list_name, start, end)

    def lpop(self, list_name):
        return self.r.lpop(list_name)
