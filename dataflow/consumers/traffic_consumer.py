from kafka import KafkaConsumer
'''서울시 실시간 도로 소통 정보'''

traffic_consumer = KafkaConsumer(
    "realtime_trafficInfo", 
    bootstrap_servers="kafka:9092",
    group_id="traffic_group",
    auto_offset_reset="earliest",
    enable_auto_commit=True
)

print("🚗 Traffic Group Listening...")
for msg in traffic_consumer:
    print(f"[{msg.topic}] {msg.value.decode('utf-8')}")
