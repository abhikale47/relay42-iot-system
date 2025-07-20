#!/bin/bash

# Simple IoT System Test Environment Setup
echo "Setting up IoT Test Environment..."

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    echo "Waiting for $service_name to be ready on port $port..."
    while [ $attempt -le $max_attempts ]; do
        if nc -z localhost $port 2>/dev/null; then
            echo "$service_name is ready!"
            return 0
        fi
        echo "Attempt $attempt/$max_attempts: $service_name not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "ERROR: $service_name failed to start within expected time"
    return 1
}

# Start Docker services
echo "Starting Docker services..."
docker-compose down -v
# Remove any conflicting containers
docker rm -f iot-timescaledb iot-zookeeper iot-kafka 2>/dev/null || true
docker-compose up -d

# Wait for services to be ready
wait_for_service "TimescaleDB" 5432 || exit 1
wait_for_service "Kafka" 9092 || exit 1

# Create logs directory
mkdir -p logs

# Start IoT System
echo "Starting IoT System..."
nohup mvn spring-boot:run -Dspring-boot.run.profiles=system -Dspring-boot.run.main-class=com.iot.system.IoTSystemApplication > logs/iot-system.log 2>&1 &
echo $! > logs/iot-system.pid

# Wait for system to be ready
echo "Waiting for IoT System to start..."
wait_for_service "IoT System" 8080 || exit 1

# Start IoT Simulator  
echo "Starting IoT Simulator..."
nohup mvn spring-boot:run@simulator -Dspring-boot.run.profiles=simulator > logs/iot-simulator.log 2>&1 &
echo $! > logs/iot-simulator.pid

# Wait for simulator to be ready
echo "Waiting for IoT Simulator to start..."
wait_for_service "IoT Simulator" 8081 || exit 1

sleep 120

# Get token using existing iotadmin user
echo "Getting auth token..."
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "iotadmin", "password": "iotadmin"}' | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# Generate 5 days of data
echo "Generating 5 days of test data..."
curl -s -X GET "http://localhost:8081/api/simulator/historical/generate-last-days/5/1" \
  -H "Authorization: Bearer $TOKEN" || true
sleep 300

# Refresh views
echo "token: $TOKEN"
echo "Refreshing views..."
curl -s -X POST "http://localhost:8080/api/system/views/refresh" \
  -H "Authorization: Bearer $TOKEN" || true

echo ""
echo "Test environment ready!"
echo ""
echo "Services:"
echo "  IoT System: http://localhost:8080/swagger-ui.html"
echo "  Simulator:  http://localhost:8081/swagger-ui.html"
echo "  Database:   localhost:5432 (iot_user/iot_password)"
echo ""
echo "Login: iotadmin/password"
echo ""
echo "To stop: docker-compose down && kill \$(cat logs/iot-*.pid)"