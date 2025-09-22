from kafka import KafkaConsumer
## 배치 데이터
weather_consumer = KafkaConsumer(
    "rain_topic",
    "outbreak_topic",
    bootstrap_servers="kafka:9092",
    group_id="weather_group",
    auto_offset_reset="earliest",
    enable_auto_commit=True
)

print("🌦 Weather Group Listening...")
for msg in weather_consumer:
    print(f"[{msg.topic}] {msg.value.decode('utf-8')}")
