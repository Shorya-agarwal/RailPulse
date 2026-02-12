import json
import random
import time
from datetime import datetime, timezone
from kafka import KafkaProducer
from kafka.errors import KafkaError
import math

# Configuration
KAFKA_BROKER = 'localhost:9092'
TOPIC = 'telemetry-stream'
NUM_TRAINS = 50
EVENTS_PER_SEC = 200  # Per train = 10k total

# Kafka producer (kafka-python syntax)
producer = KafkaProducer(
    bootstrap_servers=[KAFKA_BROKER],
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    key_serializer=lambda k: k.encode('utf-8'),
    acks=1,  # Wait for leader acknowledgment
    retries=3
)

# Load track topology
with open('track_topology.json') as f:
    topology = json.load(f)

class Train:
    def __init__(self, train_id):
        self.train_id = f"TRN-{train_id:04d}"
        self.waypoint_idx = 0
        self.progress = 0.0  # 0.0 to 1.0 between waypoints
        self.speed = random.uniform(60, 100)
        self.vibration = random.uniform(1.0, 5.0)
        self.engine_temp = random.uniform(80, 110)
        
    def update_position(self):
        # Move along route
        self.progress += 0.001 * (self.speed / 80)  # Faster trains move more
        
        if self.progress >= 1.0:
            self.progress = 0.0
            self.waypoint_idx = (self.waypoint_idx + 1) % (len(topology['waypoints']) - 1)
        
        # Interpolate position between waypoints
        start = topology['waypoints'][self.waypoint_idx]
        end = topology['waypoints'][self.waypoint_idx + 1]
        
        lat = start['lat'] + (end['lat'] - start['lat']) * self.progress
        lon = start['lon'] + (end['lon'] - start['lon']) * self.progress
        
        return lat, lon
    
    def update_telemetry(self, lat, lon):
        # Check if in sharp curve
        in_curve = False
        for curve in topology['sharp_curves']:
            dist = math.sqrt((lat - curve['lat'])**2 + (lon - curve['lon'])**2) * 111  # km
            if dist < curve['radius_km']:
                in_curve = True
                break
        
        # Realistic variations
        self.speed += random.uniform(-2, 2)
        self.speed = max(50, min(120, self.speed))
        
        # Spike vibration if speeding in curve (anomaly injection)
        if in_curve and self.speed > 70 and random.random() < 0.05:
            self.vibration = random.uniform(8.5, 9.5)  # Danger zone
        else:
            self.vibration += random.uniform(-0.5, 0.5)
            self.vibration = max(0.5, min(10, self.vibration))
        
        self.engine_temp += random.uniform(-1, 1)
        self.engine_temp = max(70, min(130, self.engine_temp))
        
        return {
            'train_id': self.train_id,
            'timestamp': datetime.now(timezone.utc).isoformat(),  # Fixed deprecation warning
            'latitude': round(lat, 6),
            'longitude': round(lon, 6),
            'speed_kmh': round(self.speed, 2),
            'vibration_level': round(self.vibration, 2),
            'engine_temp': round(self.engine_temp, 2)
        }

def main():
    trains = [Train(i) for i in range(NUM_TRAINS)]
    
    print(f"🚂 Starting simulation: {NUM_TRAINS} trains × {EVENTS_PER_SEC} events/sec")
    print(f"📊 Total throughput: {NUM_TRAINS * EVENTS_PER_SEC} events/sec")
    print(f"🎯 Target topic: {TOPIC}\n")
    
    interval = 1.0 / EVENTS_PER_SEC
    event_count = 0
    start_time = time.time()
    
    try:
        while True:
            for train in trains:
                lat, lon = train.update_position()
                telemetry = train.update_telemetry(lat, lon)
                
                try:
                    # kafka-python uses .send() and returns a FutureRecordMetadata
                    future = producer.send(
                        TOPIC,
                        key=train.train_id,
                        value=telemetry
                    )
                    # Optional: uncomment to wait for confirmation (adds latency)
                    # record_metadata = future.get(timeout=1)
                    
                except KafkaError as e:
                    print(f'❌ Failed to send event: {e}')
                
                event_count += 1
                
                if event_count % 5000 == 0:
                    elapsed = time.time() - start_time
                    rate = event_count / elapsed
                    print(f"✅ Sent {event_count:,} events | Rate: {rate:.0f} events/sec")
                
                time.sleep(interval)
    
    except KeyboardInterrupt:
        print("\n🛑 Shutting down simulator...")
    finally:
        producer.flush(timeout=5)  # Ensure all messages are sent
        producer.close()
        print("✅ Producer closed cleanly")

if __name__ == '__main__':
    main()