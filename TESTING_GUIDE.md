# IoT System Testing Guide

This guide provides step-by-step instructions for testing the IoT Data Processing System during technical interviews.

## Prerequisites Verification

Ensure you have the following installed:
```bash
# Check Java version (17+ required)
java -version

# Check Maven version (3.6+ required) 
mvn -version

# Check Docker and Docker Compose
docker --version
docker-compose --version
```

## Testing Workflow

### Phase 1: Infrastructure Setup

#### 1.1 Start Docker Services
```bash
# Navigate to project directory
cd /path/to/iot-system

# Start TimescaleDB and Kafka services
docker-compose up -d

# Verify services are running
docker-compose ps

# Expected output: timescaledb and kafka containers running
```

#### 1.2 Wait for Services to Initialize
```bash
# Wait for TimescaleDB to be ready (30-60 seconds)
docker-compose logs timescaledb | grep "database system is ready"

# Wait for Kafka to be ready
docker-compose logs kafka | grep "started (kafka.server.KafkaServer)"
```

### Phase 2: Application Startup

#### 2.1 Start IoT Processing Service (Terminal 1)
```bash
# Start main service on port 8080
mvn spring-boot:run -Dspring-boot.run.profiles=system

# Wait for startup message:
# "Started IoTSystemApplication in X.XXX seconds"
```

#### 2.2 Start IoT Simulator Service (Terminal 2)
```bash
# Start simulator service on port 8081
mvn spring-boot:run -Dspring-boot.run.main-class=com.iot.simulator.IoTSimulatorApplication

# Wait for startup message:
# "IoT Device Simulator started successfully on port 8081!"
```

#### 2.3 Verify Health Endpoints
```bash
# Check main service health
curl http://localhost:8080/actuator/health

# Check simulator service health  
curl http://localhost:8081/actuator/health

# Expected: {"status":"UP"}
```

### Phase 3: Data Generation

#### 3.1 Generate Historical Test Data
```bash
# Generate 1 day of data (recommended for testing)
curl -X GET "http://localhost:8081/api/simulator/historical/generate-last-days/1"

# Alternative: Generate 5 days with 5-minute intervals (faster)
curl -X GET "http://localhost:8081/api/simulator/historical/generate-last-days/5/5"

# Expected response: {"status":"started","message":"...","estimated_readings":...}
```

#### 3.2 Monitor Data Generation Progress
```bash
# Check simulator logs for progress
# You should see: "Generated X historical readings, current time: ..."
```

### Phase 4: Authentication Testing

#### 4.1 Obtain JWT Tokens
```bash
# Get user token
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"iotuser","password":"iotpass"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Get admin token
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"iotadmin","password":"iotadmin"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Verify tokens are obtained
echo "User Token: $USER_TOKEN"
echo "Admin Token: $ADMIN_TOKEN"
```

#### 4.2 Test Authentication Failure
```bash
# Test with invalid credentials
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"invalid","password":"wrong"}'

# Expected: 401 Unauthorized
```

### Phase 5: API Endpoint Testing

#### 5.1 Test Raw Data Queries
```bash
# Get raw sensor readings (use current time range)
curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/THERMO_001/readings?startTime=2025-07-19T10:00:00&endTime=2025-07-19T11:00:00"

# Test zone-based queries
curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/group/ZONE_A/readings?startTime=2025-07-19T10:00:00&endTime=2025-07-19T11:00:00"
```

#### 5.2 Test Aggregate Queries
```bash
# Test normal aggregates (exact query)
curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/THERMO_001/aggregates?startTime=2025-07-19T09:00:00&endTime=2025-07-19T12:00:00"

# Test partition aggregates (advanced TimescaleDB functions)
curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/THERMO_001/aggregates/partition?startTime=2025-07-19T08:00:00&endTime=2025-07-19T14:00:00"
```

#### 5.3 Test Authorization
```bash
# Test user access to admin endpoint (should fail)
curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/system/views/refresh"

# Expected: 403 Forbidden

# Test admin access to admin endpoint (should succeed)
curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
  "http://localhost:8080/api/system/views/refresh"

# Expected: Success with refresh status
```

### Phase 6: Advanced Features Testing

#### 6.1 Test Different Query Types
```bash
# Normal query - exact percentiles on raw data
curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/THERMO_001/aggregates?startTime=2025-07-19T10:00:00&endTime=2025-07-19T14:00:00"

# Partition query - advanced TimescaleDB functions
curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/THERMO_001/aggregates/partition?startTime=2025-07-19T10:00:00&endTime=2025-07-19T14:00:00"
```

#### 6.2 Test Database Management
```bash
# Refresh continuous aggregates (admin only)
curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
  "http://localhost:8080/api/system/views/refresh"
```

### Phase 7: Interactive Testing with Swagger

#### 7.1 Access Swagger UIs
- **Main Service**: http://localhost:8080/swagger-ui.html
- **Simulator Service**: http://localhost:8081/swagger-ui.html

#### 7.2 Test with Swagger
1. Click "Authorize" button in Swagger UI
2. Enter: `Bearer YOUR_JWT_TOKEN_HERE`
3. Test various endpoints interactively
4. Observe request/response formats

### Phase 8: Performance Validation

#### 8.1 Compare Query Performance
```bash
# Time normal query (exact percentiles)
time curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/THERMO_001/aggregates?startTime=2025-07-19T08:00:00&endTime=2025-07-19T16:00:00"

# Time partition query (approximate percentiles with continuous aggregates)
time curl -H "Authorization: Bearer $USER_TOKEN" \
  "http://localhost:8080/api/sensors/THERMO_001/aggregates/partition?startTime=2025-07-19T08:00:00&endTime=2025-07-19T16:00:00"
```

#### 8.2 Validate Data Accuracy
```bash
# Compare results between normal and partition queries
# Normal query provides exact results, partition query provides fast approximations
```

## Expected Results

### Successful API Response Examples

**Normal Query Response** (`/aggregates`):
```json
{
  "deviceId": "THERMO_001",
  "zone": "ZONE_A", 
  "startTime": "2025-07-19T09:00:00",
  "endTime": "2025-07-19T12:00:00",
  "average": 23.45,
  "minimum": 18.2,
  "maximum": 28.9,
  "median": 23.5,
  "dataPointCount": 1440
}
```

**Partition Query Response** (`/aggregates/partition`):
```json
{
  "deviceId": "THERMO_001",
  "zone": "ZONE_A", 
  "startTime": "2025-07-19T09:00:00",
  "endTime": "2025-07-19T12:00:00",
  "average": 23.45,
  "minimum": 18.2,
  "maximum": 28.9,
  "median": 23.5,
  "q1": 21.2,
  "q3": 25.8,
  "p95": 27.6,
  "standardDeviation": 2.1,
  "dataPointCount": 1440
}
```
