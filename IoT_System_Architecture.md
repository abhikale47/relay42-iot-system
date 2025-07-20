# IoT Data Processing System - Architecture Documentation

## System Overview

The IoT Data Processing System is a microservices-based solution built with Spring Boot 3.2.1 that processes real-time sensor data from IoT devices. The system consists of two main components: an IoT Simulator and an IoT System processor.

## High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│  IoT Simulator  │───▶│   Apache Kafka  │───▶│   IoT System    │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │                        │
                               │                        ▼
                               │              ┌─────────────────┐
                               │              │                 │
                               │              │  TimescaleDB    │
                               │              │   (PostgreSQL)  │
                               │              │                 │
                               │              └─────────────────┘
                               │
                               ▼
                     ┌─────────────────┐
                     │                 │
                     │  Dead Letter    │
                     │    Queue        │
                     │                 │
                     └─────────────────┘
```

## Data Flow Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Device    │────▶│   Kafka     │────▶│   Consumer  │────▶│ TimescaleDB │
│ Simulators  │     │   Topic     │     │ Batch Proc. │     │ Hypertable  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                           │                   │                     │
                    ┌─────────────┐     ┌─────────────┐            │
                    │  __consumer │     │ Kafka Native│            │
                    │  _offsets   │     │  Batching   │            │
                    └─────────────┘     └─────────────┘            │
                                                                   │
                                        ┌─────────────┐            │
                                        │ Continuous  │◀───────────┘
                                        │ Aggregates  │
                                        └─────────────┘
                                               │
                                        ┌─────────────┐
                                        │  REST API   │
                                        │  Queries    │
                                        └─────────────┘
```

## Component Architecture

### 1. IoT Simulator
- **Purpose**: Generates realistic sensor data for testing and development
- **Key Features**:
  - Multiple device types (Temperature, Humidity, Fuel Level)
  - Zone-based device distribution
  - Configurable data generation intervals
  - Historical data generation capabilities

### 2. IoT System Processor
- **Purpose**: Processes incoming sensor data and provides REST APIs
- **Key Features**:
  - Real-time data processing via Kafka
  - Advanced statistical aggregations
  - REST API for data retrieval
  - JWT-based authentication
  - TimescaleDB for time-series optimization

### 3. Apache Kafka
- **Topic**: `iot-readings`
- **Consumer Group**: `iot-processing-group-v2`
- **Offset Management**: Native Kafka storage (`__consumer_offsets` topic)
- **Batch Acknowledgment**: Commits offsets after entire batch processes successfully
- **Native Batching**: Uses Kafka's built-in batch processing capabilities
- **Features**:
  - Retry mechanism with exponential backoff
  - Dead Letter Queue for failed messages
  - Partitioned for scalability

### 4. TimescaleDB (PostgreSQL)
- **Purpose**: Time-series optimized database storage
- **Key Tables**:
  - `sensor_readings`: Raw sensor data
  - Materialized views for aggregated data
- **Advanced Features**:
  - Native percentile calculations
  - Time-based partitioning
  - Continuous aggregates

## API Architecture

### Authentication Endpoints
```
POST /api/auth/login
POST /api/auth/register
```

### System Data Endpoints
```
# Authentication
POST /api/auth/login

# Raw Sensor Readings
GET /api/sensors/{deviceId}/readings
GET /api/sensors/group/{zone}/readings
GET /api/sensors/type/{deviceType}/readings

# Aggregated Statistics - Normal Query
GET /api/sensors/{deviceId}/aggregates
GET /api/sensors/group/{zone}/aggregates

# Aggregated Statistics - Partition Query
GET /api/sensors/{deviceId}/aggregates/partition
GET /api/sensors/group/{zone}/aggregates/partition

# System Management
POST /api/system/views/refresh
```

### Simulator Endpoints
```
GET /api/simulator/historical/generate-last-days/{days}
GET /api/simulator/historical/generate-last-days/{days}/{intervalMinutes}
```

## Data Model

### SensorReading Entity
```java
{
  "id": Long,
  "deviceId": String,
  "deviceType": String,
  "zone": String,
  "value": Double,
  "timestamp": LocalDateTime
}
```

### Aggregate Response
```java
{
  "deviceId": String,
  "zone": String,
  "startTime": LocalDateTime,
  "endTime": LocalDateTime,
  "average": Double,
  "minimum": Double,
  "maximum": Double,
  "median": Double,
  "q1": Double,
  "q3": Double,
  "p95": Double,
  "dataPointCount": Integer
}
```

## Performance Optimizations

### Database-Native Statistics
- **Problem**: Java-based median calculation was fetching thousands of records
- **Solution**: PostgreSQL `percentile_cont()` functions for all statistics

### Kafka Configuration
- **Retry Strategy**: 3 attempts with exponential backoff
- **Dead Letter Queue**: Automatic handling of permanently failed messages
- **Partitioning**: Device ID-based partitioning for parallel processing
- **Batch Processing**: Kafka native batching with stateless design
- **Offset Management**: No external coordination required

### Time-Series Optimizations
- **TimescaleDB**: Automatic time-based partitioning
- **Continuous Aggregates**: Pre-computed hourly statistics
- **Indexing**: Optimized for time-range queries

## Query Architecture: Normal vs Partition Query Design

The system implements two distinct query strategies for aggregate statistics, each optimized for different use cases and performance characteristics.

### Normal Query Strategy

**Purpose**: Provides exact statistical calculations regardless of data volume or time range.

**Implementation**: `SensorQueryService`
- Uses PostgreSQL's native `percentile_cont()` functions
- Single database query with all statistics calculated in one operation
- Database-native calculations for optimal performance

**SQL Example**:
```sql
SELECT 
    MIN(value) as min_value,
    MAX(value) as max_value,
    AVG(value) as avg_value,
    percentile_cont(0.5) WITHIN GROUP (ORDER BY value) as median_value,
    percentile_cont(0.25) WITHIN GROUP (ORDER BY value) as q1_value,
    percentile_cont(0.75) WITHIN GROUP (ORDER BY value) as q3_value,
    percentile_cont(0.95) WITHIN GROUP (ORDER BY value) as p95_value,
    COUNT(*) as count_value
FROM sensor_readings 
WHERE device_id = ? AND timestamp BETWEEN ? AND ?
```

**Characteristics**:
- **Accuracy**: 100% exact percentiles
- **Performance**: O(n log n) for large datasets due to sorting requirements
- **Best For**: Small to medium datasets, exact analytics requirements

### Partition Query Strategy

**Purpose**: Provides approximate statistical calculations optimized for massive datasets using TimescaleDB advanced aggregation functions.

**Implementation**: `SmartPartitionQueryService`
- Uses TimescaleDB's `approx_percentile_agg()` and `rollup()` functions
- Leverages continuous aggregates and time-based partitioning
- Approximate algorithms for statistical calculations

**SQL Example**:
```sql
SELECT 
    min(min_value) as overall_min,
    max(max_value) as overall_max,
    avg(avg_value) as overall_avg,
    approx_percentile(0.5, rollup(percentile_agg)) as approx_median,
    approx_percentile(0.25, rollup(percentile_agg)) as approx_q1,
    approx_percentile(0.75, rollup(percentile_agg)) as approx_q3,
    approx_percentile(0.95, rollup(percentile_agg)) as approx_p95,
    sum(count_agg) as total_count
FROM sensor_readings_hourly_agg
WHERE device_id = ? AND bucket BETWEEN ? AND ?
GROUP BY device_id
```

**Characteristics**:
- **Accuracy**: ~99.9% accurate percentiles (t-digest algorithm)
- **Performance**: Significantly faster than normal query by using pre-computed aggregates
- **Memory Usage**: Very low - operates on pre-aggregated data
- **Best For**: Large datasets

### Partition Query with Non-Aligned Time Ranges

When query time ranges don't align with hourly aggregate boundaries, the partition query uses a hybrid approach:

**Challenge**: Query from `2024-01-15 14:30:00` to `2024-01-17 09:45:00` 

**Solution**: Three-part query strategy:
1. **Raw data for partial first hour** (14:30-15:00)
2. **Pre-computed aggregates for full hours** (15:00-09:00) 
3. **Raw data for partial last hour** (09:00-09:45)

**Implementation Example**:
```sql
-- Part 1: Raw data for first partial hour
SELECT percentile_agg(value) FROM sensor_readings 
WHERE device_id = ? AND timestamp BETWEEN '2024-01-15 14:30:00' AND '2024-01-15 15:00:00'

UNION ALL

-- Part 2: Pre-computed hourly aggregates
SELECT rollup(percentile_agg) FROM sensor_readings_hourly_agg
WHERE device_id = ? AND bucket BETWEEN '2024-01-15 15:00:00' AND '2024-01-17 09:00:00'

UNION ALL

-- Part 3: Raw data for last partial hour  
SELECT percentile_agg(value) FROM sensor_readings
WHERE device_id = ? AND timestamp BETWEEN '2024-01-17 09:00:00' AND '2024-01-17 09:45:00'
```

**Performance Impact**:
- **Hour-aligned queries**: Maximum benefit from pre-computed aggregates
- **Non-aligned queries**: Reduced benefit due to partial raw data processing
- **Trade-off**: Maintains accuracy while leveraging aggregates where possible

### Query Strategy Selection Logic

The system provides both strategies as separate endpoints, allowing clients to choose based on their specific requirements:

**Normal Query Endpoints**:
- `GET /api/sensors/{deviceId}/aggregates`
- `GET /api/sensors/group/{zone}/aggregates`

**Partition Query Endpoints**:
- `GET /api/sensors/{deviceId}/aggregates/partition`
- `GET /api/sensors/group/{zone}/aggregates/partition`

### Continuous Aggregates Design

The partition query strategy leverages TimescaleDB continuous aggregates:

```sql
CREATE MATERIALIZED VIEW sensor_readings_hourly_agg
WITH (timescaledb.continuous) AS
SELECT 
    time_bucket('1 hour', timestamp) AS bucket,
    device_id,
    zone,
    device_type,
    min(value) as min_value,
    max(value) as max_value,
    avg(value) as avg_value,
    approx_percentile_agg(value) as percentile_agg,
    variance_agg(value) as variance_agg,
    count(*) as count_agg
FROM sensor_readings
GROUP BY bucket, device_id, zone, device_type;
```

**Benefits**:
- **Pre-computation**: Statistics calculated incrementally as data arrives
- **Storage Efficiency**: Compressed representation of statistical distributions
- **Query Speed**: Sub-second response for any time range
- **Automatic Maintenance**: TimescaleDB handles refresh scheduling

## Security Features

### JWT Authentication
- **Algorithm**: HMAC-SHA256
- **Claims**: Username, roles, expiration
- **Token Validity**: Configurable (default: 24 hours)

### Role-Based Access Control
- **USER**: Read-only access to data endpoints
- **ADMIN**: Full access including system management
- **Endpoint Security**: Method-level security annotations

## Deployment Architecture

### Docker Compose Services
1. **TimescaleDB**: PostgreSQL with TimescaleDB extension
2. **Apache Kafka**: Message broker with Zookeeper
3. **IoT System**: Main processing application
4. **IoT Simulator**: Data generation service

### Monitoring and Observability
- **Spring Boot Actuator**: Health checks and metrics
- **Kafka Consumer Groups**: Processing status monitoring
- **Database Metrics**: Connection pool and query performance

## Error Handling and Resilience

### Kafka Retry Mechanism
```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2.0),
    dltStrategy = DltStrategy.FAIL_ON_ERROR
)
```

## Scalability Considerations

### Horizontal Scaling
- **Kafka Partitions**: Multiple consumers for parallel processing
- **Database Sharding**: Time-based partitioning in TimescaleDB
- **Stateless Services**: No shared state or locks - unlimited horizontal scaling
- **Independent Processing**: Each service instance operates independently
- **Zero Coordination**: No inter-service synchronization required

### Vertical Scaling
- **Connection Pooling**: HikariCP for database connections
- **JVM Optimization**: Heap and GC tuning
- **Database Indexing**: Query optimization strategies

---
