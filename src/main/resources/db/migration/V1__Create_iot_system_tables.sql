-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS timescaledb;
CREATE EXTENSION IF NOT EXISTS timescaledb_toolkit;

-- Create main hypertable for sensor readings
CREATE TABLE sensor_readings (
                                 id BIGSERIAL,
                                 device_id VARCHAR(100) NOT NULL,
                                 device_type VARCHAR(50) NOT NULL,
                                 zone VARCHAR(50) NOT NULL,
                                 value DOUBLE PRECISION NOT NULL,
                                 timestamp TIMESTAMP NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Convert to hypertable
SELECT create_hypertable('sensor_readings', 'timestamp');

-- Primary key
ALTER TABLE sensor_readings ADD CONSTRAINT sensor_readings_pkey PRIMARY KEY (id, timestamp);

-- Indexes for query optimization
CREATE INDEX idx_sensor_readings_device_id_timestamp ON sensor_readings (device_id, timestamp DESC);
CREATE INDEX idx_sensor_readings_zone_timestamp ON sensor_readings (zone, timestamp DESC);
CREATE INDEX idx_sensor_readings_device_type_timestamp ON sensor_readings (device_type, timestamp DESC);
CREATE INDEX idx_sensor_readings_value_timestamp ON sensor_readings (value, timestamp DESC);

-- Enable compression
ALTER TABLE sensor_readings SET (timescaledb.compress = true);
SELECT add_compression_policy('sensor_readings', INTERVAL '7 day');

-- Create device-level continuous aggregates
CREATE MATERIALIZED VIEW device_hourly_aggregates
WITH (
    timescaledb.continuous,
    timescaledb.materialized_only = true
) AS
SELECT
    device_id,
    device_type,
    zone,
    time_bucket('1 hour', timestamp) AS time_bucket,
    COUNT(*) AS data_point_count,
    AVG(value) AS average_value,
    MIN(value) AS min_value,
    MAX(value) AS max_value,
    percentile_agg(value) AS percentile_summary,
    stats_agg(value) AS stats_summary,
    percentile_cont(0.5) WITHIN GROUP (ORDER BY value) AS median_value,
    percentile_cont(0.25) WITHIN GROUP (ORDER BY value) AS q1_value,
    percentile_cont(0.75) WITHIN GROUP (ORDER BY value) AS q3_value,
    percentile_cont(0.95) WITHIN GROUP (ORDER BY value) AS p95_value
FROM sensor_readings
GROUP BY device_id, device_type, zone, time_bucket('1 hour', timestamp)
WITH NO DATA;



-- Create zone-level continuous aggregates
CREATE MATERIALIZED VIEW zone_hourly_aggregates
WITH (
    timescaledb.continuous,
    timescaledb.materialized_only = true
) AS
SELECT
    zone,
    time_bucket('1 hour', timestamp) AS time_bucket,
    COUNT(*) AS data_point_count,
    AVG(value) AS average_value,
    MIN(value) AS min_value,
    MAX(value) AS max_value,
    percentile_agg(value) AS percentile_summary,
    stats_agg(value) AS stats_summary,
    percentile_cont(0.5) WITHIN GROUP (ORDER BY value) AS median_value,
    percentile_cont(0.25) WITHIN GROUP (ORDER BY value) AS q1_value,
    percentile_cont(0.75) WITHIN GROUP (ORDER BY value) AS q3_value,
    percentile_cont(0.95) WITHIN GROUP (ORDER BY value) AS p95_value,
    COUNT(DISTINCT device_id) AS unique_devices,
    COUNT(DISTINCT device_type) AS unique_device_types
FROM sensor_readings
GROUP BY zone, time_bucket('1 hour', timestamp)
WITH NO DATA;



-- Add continuous aggregate refresh policies
SELECT add_continuous_aggregate_policy('device_hourly_aggregates',
                                       start_offset => INTERVAL '3 hours',
                                       end_offset => INTERVAL '0 hour',
                                       schedule_interval => INTERVAL '30 minutes');

SELECT add_continuous_aggregate_policy('zone_hourly_aggregates',
                                       start_offset => INTERVAL '3 hours',
                                       end_offset => INTERVAL '0 hour',
                                       schedule_interval => INTERVAL '30 minutes');

-- Indexes on continuous aggregates
CREATE INDEX idx_device_hourly_agg_device_time ON device_hourly_aggregates (device_id, time_bucket DESC);
CREATE INDEX idx_zone_hourly_agg_zone_time ON zone_hourly_aggregates (zone, time_bucket DESC);

-- Retention policies
SELECT add_retention_policy('sensor_readings', INTERVAL '1 year');
SELECT add_retention_policy('device_hourly_aggregates', INTERVAL '2 years');
SELECT add_retention_policy('zone_hourly_aggregates', INTERVAL '2 years');

-- Permissions
GRANT SELECT, INSERT, UPDATE ON sensor_readings TO PUBLIC;
GRANT SELECT ON device_hourly_aggregates TO PUBLIC;
GRANT SELECT ON zone_hourly_aggregates TO PUBLIC;
