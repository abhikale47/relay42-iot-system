-- Enable TimescaleDB and TimescaleDB Toolkit extensions
-- This script runs automatically when the container starts

-- Connect to the iot_system database
\c iot_system;

-- Enable TimescaleDB extension
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Enable TimescaleDB Toolkit extension (provides advanced functions)
CREATE EXTENSION IF NOT EXISTS timescaledb_toolkit;

-- Verify extensions are loaded
SELECT extname, extversion FROM pg_extension WHERE extname IN ('timescaledb', 'timescaledb_toolkit');

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'TimescaleDB and TimescaleDB Toolkit extensions enabled successfully';
END $$;