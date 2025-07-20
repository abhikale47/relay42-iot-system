package com.iot.system.controller;

import com.iot.common.model.SensorReading;
import com.iot.system.dto.SensorAggregateData;
import com.iot.system.service.SensorQueryService;
import com.iot.system.service.SmartPartitionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Sensor Data",
    description = "IoT sensor data query and analytics API with advanced TimescaleDB features")
@RestController
@RequestMapping("/api/sensors")
@PreAuthorize("hasRole('USER')")
@Profile("system")
public class SensorController {

  @Autowired private SensorQueryService sensorQueryService;

  @Autowired private SmartPartitionQueryService smartPartitionQueryService;

  @Operation(
      summary = "Get device sensor readings",
      description = "Retrieve raw sensor readings for a specific device within a time range")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved readings"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
        @ApiResponse(responseCode = "404", description = "Device not found")
      })
  @SecurityRequirement(name = "bearer-jwt")
  @GetMapping("/{deviceId}/readings")
  public ResponseEntity<List<SensorReading>> getDeviceReadings(
      @Parameter(
              description = "Device identifier (e.g., THERMO_001, HR_001)",
              example = "THERMO_001")
          @PathVariable
          String deviceId,
      @Parameter(description = "Start time in ISO format", example = "2025-07-18T10:00:00")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startTime,
      @Parameter(description = "End time in ISO format", example = "2025-07-18T15:00:00")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endTime) {

    List<SensorReading> readings =
        sensorQueryService.getDeviceReadings(deviceId, startTime, endTime);
    return ResponseEntity.ok(readings);
  }

  @SecurityRequirement(name = "bearer-jwt")
  @GetMapping("/group/{zone}/readings")
  public ResponseEntity<List<SensorReading>> getZoneReadings(
      @PathVariable String zone,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

    List<SensorReading> readings = sensorQueryService.getZoneReadings(zone, startTime, endTime);
    return ResponseEntity.ok(readings);
  }

  @SecurityRequirement(name = "bearer-jwt")
  @GetMapping("/type/{deviceType}/readings")
  public ResponseEntity<List<SensorReading>> getDeviceTypeReadings(
      @PathVariable String deviceType,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

    List<SensorReading> readings =
        sensorQueryService.getDeviceTypeReadings(deviceType, startTime, endTime);
    return ResponseEntity.ok(readings);
  }

  @Operation(
      summary = "Get device aggregated statistics",
      description = "Get aggregated statistics for a device using normal query (exact percentiles)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved aggregates"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
      })
  @SecurityRequirement(name = "bearer-jwt")
  @GetMapping("/{deviceId}/aggregates")
  public ResponseEntity<SensorAggregateData> getDeviceAggregates(
      @Parameter(description = "Device identifier", example = "THERMO_001") @PathVariable
          String deviceId,
      @Parameter(description = "Start time in ISO format", example = "2025-07-18T10:00:00")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startTime,
      @Parameter(description = "End time in ISO format", example = "2025-07-18T15:00:00")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endTime) {

    // Use normal query - exact percentiles regardless of time range
    SensorAggregateData aggregates =
        sensorQueryService.getDeviceAggregates(deviceId, startTime, endTime);
    return ResponseEntity.ok(aggregates);
  }

  @SecurityRequirement(name = "bearer-jwt")
  @GetMapping("/group/{zone}/aggregates")
  public ResponseEntity<SensorAggregateData> getZoneAggregates(
      @PathVariable String zone,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

    // Use normal query - exact percentiles regardless of time range
    SensorAggregateData aggregates = sensorQueryService.getZoneAggregates(zone, startTime, endTime);
    return ResponseEntity.ok(aggregates);
  }

  // Smart partition query endpoints using TimescaleDB advanced aggregation

  @Operation(
      summary = "Get device aggregates using partition query",
      description =
          "Force use of partition query with TimescaleDB advanced functions (approx_percentile_agg, rollup)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved partition aggregates"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required")
      })
  @SecurityRequirement(name = "bearer-jwt")
  @GetMapping("/{deviceId}/aggregates/partition")
  public ResponseEntity<SensorAggregateData> getDeviceAggregatesWithPartitions(
      @Parameter(description = "Device identifier", example = "THERMO_001") @PathVariable
          String deviceId,
      @Parameter(description = "Start time in ISO format", example = "2025-07-18T10:00:00")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startTime,
      @Parameter(description = "End time in ISO format", example = "2025-07-18T15:00:00")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endTime) {

    // Force partition query - always use advanced TimescaleDB functions
    SensorAggregateData aggregates =
        smartPartitionQueryService.getPartitionedDeviceAggregates(deviceId, startTime, endTime);
    return ResponseEntity.ok(aggregates);
  }

  @SecurityRequirement(name = "bearer-jwt")
  @GetMapping("/group/{zone}/aggregates/partition")
  public ResponseEntity<SensorAggregateData> getZoneAggregatesWithPartitions(
      @PathVariable String zone,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

    // Force partition query - always use advanced TimescaleDB functions
    SensorAggregateData aggregates =
        smartPartitionQueryService.getPartitionedZoneAggregates(zone, startTime, endTime);
    return ResponseEntity.ok(aggregates);
  }
}
