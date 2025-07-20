# IoT Data Processing System

A high-performance IoT data processing system built with Spring Boot and TimescaleDB for real-time sensor data analytics.

> **📖 For detailed system architecture, query strategies, and performance analysis, see [IoT_System_Architecture.md](IoT_System_Architecture.md)**

## Quick Setup for Testing

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.6+

### One-Command Setup
```bash
./setup-test-environment.sh
```

This script will:
1. Start Docker services (TimescaleDB + Kafka)
2. Start IoT System (port 8080) and Simulator (port 8081) 
3. Generate 5 days of test data
4. Refresh materialized views

### Manual Setup (Alternative)

1. **Start Docker Services**
```bash
docker-compose up -d
```

2. **Start Applications**
```bash
# Terminal 1: IoT System
mvn spring-boot:run -Dspring-boot.run.profiles=system

# Terminal 2: IoT Simulator  
mvn spring-boot:run -Dspring-boot.run.profiles=simulator
```

3. **Generate Test Data**
```bash
# Get auth token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"iotadmin","password":"password"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Generate 5 days of data
curl -X POST "http://localhost:8081/api/simulator/historical/generate-last-days/5" \
  -H "Authorization: Bearer $TOKEN"
```

## Testing the APIs

### Interactive Testing (Recommended)
- **IoT System**: http://localhost:8080/swagger-ui.html
- **Simulator**: http://localhost:8081/swagger-ui.html

**Login**: iotadmin/password

### Sample API Calls
```bash
# Get device readings
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/sensors/TEMP_001/readings?startTime=2024-01-01T10:00:00&endTime=2024-01-01T11:00:00"

# Get aggregates (normal query - exact percentiles)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/sensors/TEMP_001/aggregates?startTime=2024-01-01T09:00:00&endTime=2024-01-01T12:00:00"

# Get aggregates (partition query - approximate, faster)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/sensors/TEMP_001/aggregates/partition?startTime=2024-01-01T09:00:00&endTime=2024-01-01T12:00:00"
```

## Key Endpoints

### Data Retrieval
- `GET /api/sensors/{deviceId}/readings` - Raw sensor data
- `GET /api/sensors/{deviceId}/aggregates` - Exact statistics 
- `GET /api/sensors/{deviceId}/aggregates/partition` - Approximate statistics (faster)
- `GET /api/sensors/group/{zone}/aggregates` - Zone-level aggregates

### Data Generation
- `GET /api/simulator/historical/generate-last-days/{days}` - Generate test data

## Services
- **IoT System**: http://localhost:8080 (API + Processing)
- **Simulator**: http://localhost:8081 (Data Generation)
- **Database**: localhost:5432 (iot_user/iot_password)

## Stopping Services
```bash
docker-compose down && kill $(cat logs/iot-*.pid)
```

## Architecture Details

For comprehensive information about:
- System architecture and components
- Normal vs Partition query strategies
- Performance characteristics
- Security features
- Deployment considerations

See **[IoT_System_Architecture.md](IoT_System_Architecture.md)**