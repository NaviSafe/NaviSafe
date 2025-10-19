import time
from redis_utils import RedisClient

redis_client = RedisClient(host="redis", port=6379, db=0)

# items = redis_client.lrange("db_queue", 0, -1)
# print(items)
item = redis_client.lrange("link_queue", 0, -1)
print(item)
time.sleep(5)
