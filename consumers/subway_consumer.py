from kafka import KafkaConsumer
## 실시간 지하철 위치 데이터
subway_consumer = KafkaConsumer(
    "subway_position_topic",
    bootstrap_servers="kafka:9092",
    group_id="subway_group",
    auto_offset_reset="earliest",
    enable_auto_commit=True
)

print("🚇 Subway Group Listening...")
for msg in subway_consumer:
    print(f"[{msg.topic}] {msg.value.decode('utf-8')}")
