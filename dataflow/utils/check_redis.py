import time
from redis_utils import RedisClient

redis_client = RedisClient(host="redis", port=6379, db=0)

while True:
    items = redis_client.lrange("db_queue", 0, -1)
    print(items)
    time.sleep(5)
