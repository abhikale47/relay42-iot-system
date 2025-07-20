package com.iot.system.repository;

import com.iot.common.model.SensorReading;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

  @Query(
      "SELECT sr FROM SensorReading sr WHERE sr.deviceId = :deviceId "
          + "AND sr.timestamp BETWEEN :startTime AND :endTime ORDER BY sr.timestamp DESC")
  List<SensorReading> findByDeviceIdAndTimestampBetween(
      @Param("deviceId") String deviceId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      "SELECT sr FROM SensorReading sr WHERE sr.zone = :zone "
          + "AND sr.timestamp BETWEEN :startTime AND :endTime ORDER BY sr.timestamp DESC")
  List<SensorReading> findByZoneAndTimestampBetween(
      @Param("zone") String zone,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      "SELECT sr FROM SensorReading sr WHERE sr.deviceType = :deviceType "
          + "AND sr.timestamp BETWEEN :startTime AND :endTime ORDER BY sr.timestamp DESC")
  List<SensorReading> findByDeviceTypeAndTimestampBetween(
      @Param("deviceType") String deviceType,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      value =
          "SELECT "
              + "MIN(value) as min_value, "
              + "MAX(value) as max_value, "
              + "AVG(value) as avg_value, "
              + "percentile_cont(0.5) WITHIN GROUP (ORDER BY value) as median_value, "
              + "percentile_cont(0.25) WITHIN GROUP (ORDER BY value) as q1_value, "
              + "percentile_cont(0.75) WITHIN GROUP (ORDER BY value) as q3_value, "
              + "percentile_cont(0.95) WITHIN GROUP (ORDER BY value) as p95_value, "
              + "COUNT(*) as count_value "
              + "FROM sensor_readings "
              + "WHERE device_id = :deviceId AND timestamp BETWEEN :startTime AND :endTime",
      nativeQuery = true)
  Object[][] findCompleteAggregatesByDeviceIdAndTimestampBetween(
      @Param("deviceId") String deviceId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      value =
          "SELECT "
              + "MIN(value) as min_value, "
              + "MAX(value) as max_value, "
              + "AVG(value) as avg_value, "
              + "percentile_cont(0.5) WITHIN GROUP (ORDER BY value) as median_value, "
              + "percentile_cont(0.25) WITHIN GROUP (ORDER BY value) as q1_value, "
              + "percentile_cont(0.75) WITHIN GROUP (ORDER BY value) as q3_value, "
              + "percentile_cont(0.95) WITHIN GROUP (ORDER BY value) as p95_value, "
              + "COUNT(*) as count_value "
              + "FROM sensor_readings "
              + "WHERE zone = :zone AND timestamp BETWEEN :startTime AND :endTime",
      nativeQuery = true)
  Object[][] findCompleteAggregatesByZoneAndTimestampBetween(
      @Param("zone") String zone,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  // Helper methods to convert Object[] to SensorAggregateResult
  default SensorAggregateResult findAggregatesByDeviceIdAndTimestampBetween(
      String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
    Object[][] result =
        findCompleteAggregatesByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
    if (result == null || result.length == 0) {
      return new SensorAggregateResult(null, null, null, null, null, null, null, 0);
    }
    Object[] row = result[0];
    return new SensorAggregateResult(
        row[0] != null ? ((Number) row[0]).doubleValue() : null, // min
        row[1] != null ? ((Number) row[1]).doubleValue() : null, // max
        row[2] != null ? ((Number) row[2]).doubleValue() : null, // avg
        row[3] != null ? ((Number) row[3]).doubleValue() : null, // median
        row[4] != null ? ((Number) row[4]).doubleValue() : null, // q1
        row[5] != null ? ((Number) row[5]).doubleValue() : null, // q3
        row[6] != null ? ((Number) row[6]).doubleValue() : null, // p95
        row[7] != null ? ((Number) row[7]).intValue() : 0 // count
        );
  }

  default SensorAggregateResult findAggregatesByZoneAndTimestampBetween(
      String zone, LocalDateTime startTime, LocalDateTime endTime) {
    Object[][] result = findCompleteAggregatesByZoneAndTimestampBetween(zone, startTime, endTime);
    if (result == null || result.length == 0) {
      return new SensorAggregateResult(null, null, null, null, null, null, null, 0);
    }
    Object[] row = result[0];
    return new SensorAggregateResult(
        row[0] != null ? ((Number) row[0]).doubleValue() : null, // min
        row[1] != null ? ((Number) row[1]).doubleValue() : null, // max
        row[2] != null ? ((Number) row[2]).doubleValue() : null, // avg
        row[3] != null ? ((Number) row[3]).doubleValue() : null, // median
        row[4] != null ? ((Number) row[4]).doubleValue() : null, // q1
        row[5] != null ? ((Number) row[5]).doubleValue() : null, // q3
        row[6] != null ? ((Number) row[6]).doubleValue() : null, // p95
        row[7] != null ? ((Number) row[7]).intValue() : 0 // count
        );
  }
}
