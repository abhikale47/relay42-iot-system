package com.iot.system.service;

import com.iot.system.dto.SensorAggregateData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("system")
public class SmartPartitionQueryService {

  private static final Logger log = LoggerFactory.getLogger(SmartPartitionQueryService.class);

  @Autowired private JdbcTemplate jdbcTemplate;

  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /** Get partitioned aggregates for device using advanced TimescaleDB functions */
  public SensorAggregateData getPartitionedDeviceAggregates(
      String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("Using partitioned query for device {}", deviceId);

    String sql = buildPartitionQuery("device_id", "device_hourly_aggregates");

    Map<String, Object> result =
        jdbcTemplate.queryForMap(
            sql,
            startTime.format(TIMESTAMP_FORMAT),
            endTime.format(TIMESTAMP_FORMAT),
            startTime.format(TIMESTAMP_FORMAT),
            endTime.format(TIMESTAMP_FORMAT),
            deviceId,
            deviceId,
            deviceId,
            deviceId);

    return mapResultToSensorAggregateData(result, deviceId, null, startTime, endTime);
  }

  /** Get partitioned aggregates for zone using advanced TimescaleDB functions */
  public SensorAggregateData getPartitionedZoneAggregates(
      String zone, LocalDateTime startTime, LocalDateTime endTime) {
    log.debug("Using partitioned query for zone {}", zone);

    String sql = buildPartitionQuery("zone", "zone_hourly_aggregates");

    Map<String, Object> result =
        jdbcTemplate.queryForMap(
            sql,
            startTime.format(TIMESTAMP_FORMAT),
            endTime.format(TIMESTAMP_FORMAT),
            startTime.format(TIMESTAMP_FORMAT),
            endTime.format(TIMESTAMP_FORMAT),
            zone,
            zone,
            zone,
            zone);

    return mapResultToSensorAggregateData(result, null, zone, startTime, endTime);
  }

  /** Build parametrized partition query for both device and zone aggregates */
  private String buildPartitionQuery(String filterColumn, String aggregateTable) {
    return String.format(
        """
            WITH range_window AS (
              SELECT
                ?::timestamp AS start_time,
                ?::timestamp AS end_time,
                time_bucket('1 hour', ?::timestamp) AS start_bucket,
                time_bucket('1 hour', ?::timestamp) AS end_bucket
            ),

            -- 1. Raw value sources for partial buckets
            raw_start AS (
              SELECT value FROM sensor_readings, range_window
              WHERE %s = ?
                AND timestamp >= range_window.start_time
                AND timestamp < range_window.start_bucket + INTERVAL '1 hour'
            ),
            raw_end AS (
              SELECT value FROM sensor_readings, range_window
              WHERE %s = ?
                AND timestamp >= range_window.end_bucket
                AND timestamp < range_window.end_time
            ),

            -- 2. Pre-aggregated full buckets
            agg_middle AS (
              SELECT min_value, max_value, average_value, data_point_count
              FROM %s, range_window
              WHERE %s = ?
                AND time_bucket >= range_window.start_bucket + INTERVAL '1 hour'
                AND time_bucket < range_window.end_bucket
            ),

            -- 3. Combine percentiles from raw and pre-agg into a rollup
            all_percentiles AS (
              SELECT percentile_agg(value) AS perc FROM raw_start
              UNION ALL
              SELECT percentile_agg(value) FROM raw_end
              UNION ALL
              SELECT rollup(percentile_summary) FROM %s, range_window
              WHERE %s = ?
                AND time_bucket >= range_window.start_bucket + INTERVAL '1 hour'
                AND time_bucket < range_window.end_bucket
            ),
            final_percentile AS (
              SELECT rollup(perc) AS combined_percentile FROM all_percentiles
            ),

            -- 4. Combine raw and pre-agg stats
            all_stats AS (
              SELECT value AS min_value, value AS max_value, value AS avg_value, 1 AS count_value FROM raw_start
              UNION ALL
              SELECT value, value, value, 1 FROM raw_end
              UNION ALL
              SELECT min_value, max_value, average_value, data_point_count FROM agg_middle
            ),
            final_stats AS (
              SELECT
                MIN(min_value) AS min_value,
                MAX(max_value) AS max_value,
                ROUND((SUM(avg_value * count_value) / NULLIF(SUM(count_value), 0))::numeric, 3) AS avg_value,
                SUM(count_value) AS count_value
              FROM all_stats
            )

            -- Final single-row SELECT joining the two
            SELECT
              approx_percentile(0.5, p.combined_percentile) AS median,
              approx_percentile(0.25, p.combined_percentile) AS q1,
              approx_percentile(0.75, p.combined_percentile) AS q3,
              approx_percentile(0.95, p.combined_percentile) AS p95,
              s.min_value,
              s.max_value,
              s.avg_value,
              s.count_value
            FROM final_percentile p, final_stats s;
            """,
        filterColumn, filterColumn, aggregateTable, filterColumn, aggregateTable, filterColumn);
  }

  /** Map database query result to SensorAggregateData */
  private SensorAggregateData mapResultToSensorAggregateData(
      Map<String, Object> result,
      String deviceId,
      String zone,
      LocalDateTime startTime,
      LocalDateTime endTime) {
    Double median = getDoubleValue(result, "median");
    Double min = getDoubleValue(result, "min_value");
    Double max = getDoubleValue(result, "max_value");
    Double avg = getDoubleValue(result, "avg_value");
    Integer count = getIntegerValue(result, "count_value");

    // Additional statistics available from partition query
    Double q1 = getDoubleValue(result, "q1");
    Double q3 = getDoubleValue(result, "q3");
    Double p95 = getDoubleValue(result, "p95");

    log.debug(
        "Partition query results - count: {}, avg: {}, median: {}, min: {}, max: {}, q1: {}, q3: {}, p95: {}",
        count,
        avg,
        median,
        min,
        max,
        q1,
        q3,
        p95);

    return new SensorAggregateData(
        deviceId,
        zone,
        startTime,
        endTime,
        avg,
        min,
        max,
        median,
        q1,
        q3,
        p95,
        count != null ? count : 0);
  }

  private Double getDoubleValue(Map<String, Object> result, String key) {
    Object value = result.get(key);
    if (value == null) return null;
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return null;
  }

  private Integer getIntegerValue(Map<String, Object> result, String key) {
    Object value = result.get(key);
    if (value == null) return null;
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return null;
  }
}
