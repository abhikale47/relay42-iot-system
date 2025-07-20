package com.iot.system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.iot.common.model.SensorReading;
import com.iot.system.dto.SensorAggregateData;
import com.iot.system.repository.SensorAggregateResult;
import com.iot.system.repository.SensorReadingRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensorQueryServiceTest {

  @Mock private SensorReadingRepository sensorReadingRepository;

  @InjectMocks private SensorQueryService sensorQueryService;

  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String deviceId;
  private String zone;

  @BeforeEach
  void setUp() {
    startTime = LocalDateTime.now().minusHours(1);
    endTime = LocalDateTime.now();
    deviceId = "THERMO_001";
    zone = "ZONE_A";
  }

  @Test
  void testGetDeviceReadings() {
    List<SensorReading> expectedReadings =
        Arrays.asList(
            createSensorReading(deviceId, "THERMOSTAT", zone, 22.5),
            createSensorReading(deviceId, "THERMOSTAT", zone, 23.0));
    when(sensorReadingRepository.findByDeviceIdAndTimestampBetween(eq(deviceId), any(), any()))
        .thenReturn(expectedReadings);

    List<SensorReading> actualReadings =
        sensorQueryService.getDeviceReadings(deviceId, startTime, endTime);

    assertEquals(expectedReadings.size(), actualReadings.size());
    assertEquals(expectedReadings.get(0).getDeviceId(), actualReadings.get(0).getDeviceId());
  }

  @Test
  void testGetZoneReadings() {
    List<SensorReading> expectedReadings =
        Arrays.asList(
            createSensorReading("THERMO_001", "THERMOSTAT", zone, 22.5),
            createSensorReading("HR_001", "HEART_RATE", zone, 75.0));
    when(sensorReadingRepository.findByZoneAndTimestampBetween(eq(zone), any(), any()))
        .thenReturn(expectedReadings);

    List<SensorReading> actualReadings =
        sensorQueryService.getZoneReadings(zone, startTime, endTime);

    assertEquals(expectedReadings.size(), actualReadings.size());
    assertEquals(zone, actualReadings.get(0).getZone());
  }

  @Test
  void testGetDeviceAggregates() {
    SensorAggregateResult mockResult = new SensorAggregateResult(22.0, 23.5, 22.75, 22.5, 21.0, 23.0, 23.2, 4);
    when(sensorReadingRepository.findAggregatesByDeviceIdAndTimestampBetween(
            eq(deviceId), any(), any()))
        .thenReturn(mockResult);

    SensorAggregateData aggregates =
        sensorQueryService.getDeviceAggregates(deviceId, startTime, endTime);

    assertNotNull(aggregates);
    assertEquals(deviceId, aggregates.getDeviceId());
    assertEquals(22.75, aggregates.getAverage());
    assertEquals(22.0, aggregates.getMinimum());
    assertEquals(23.5, aggregates.getMaximum());
    assertEquals(22.5, aggregates.getMedian());
    assertEquals(4, aggregates.getDataPointCount());
  }

  @Test
  void testGetZoneAggregates() {
    SensorAggregateResult mockResult = new SensorAggregateResult(20.0, 80.0, 50.0, 45.0, 30.0, 65.0, 75.0, 4);
    when(sensorReadingRepository.findAggregatesByZoneAndTimestampBetween(eq(zone), any(), any()))
        .thenReturn(mockResult);

    SensorAggregateData aggregates = sensorQueryService.getZoneAggregates(zone, startTime, endTime);

    assertNotNull(aggregates);
    assertEquals(zone, aggregates.getZone());
    assertEquals(50.0, aggregates.getAverage());
    assertEquals(20.0, aggregates.getMinimum());
    assertEquals(80.0, aggregates.getMaximum());
    assertEquals(45.0, aggregates.getMedian());
    assertEquals(4, aggregates.getDataPointCount());
  }

  @Test
  void testGetDeviceTypeReadings() {
    String deviceType = "THERMOSTAT";
    List<SensorReading> expectedReadings =
        Arrays.asList(
            createSensorReading("THERMO_001", deviceType, "ZONE_A", 22.5),
            createSensorReading("THERMO_002", deviceType, "ZONE_B", 23.0));
    when(sensorReadingRepository.findByDeviceTypeAndTimestampBetween(eq(deviceType), any(), any()))
        .thenReturn(expectedReadings);

    List<SensorReading> actualReadings =
        sensorQueryService.getDeviceTypeReadings(deviceType, startTime, endTime);

    assertEquals(expectedReadings.size(), actualReadings.size());
    assertEquals(deviceType, actualReadings.get(0).getDeviceType());
  }

  @Test
  void testGetDeviceAggregatesWithNoData() {
    SensorAggregateResult mockResult = new SensorAggregateResult(null, null, null, null, null, null, null, 0);
    when(sensorReadingRepository.findAggregatesByDeviceIdAndTimestampBetween(
            eq(deviceId), any(), any()))
        .thenReturn(mockResult);

    SensorAggregateData aggregates =
        sensorQueryService.getDeviceAggregates(deviceId, startTime, endTime);

    assertNotNull(aggregates);
    assertEquals(deviceId, aggregates.getDeviceId());
    assertNull(aggregates.getAverage());
    assertNull(aggregates.getMinimum());
    assertNull(aggregates.getMaximum());
    assertNull(aggregates.getMedian());
    assertEquals(0, aggregates.getDataPointCount());
  }

  @Test
  void testGetZoneAggregatesWithNoData() {
    SensorAggregateResult mockResult = new SensorAggregateResult(null, null, null, null, null, null, null, 0);
    when(sensorReadingRepository.findAggregatesByZoneAndTimestampBetween(eq(zone), any(), any()))
        .thenReturn(mockResult);

    SensorAggregateData aggregates = sensorQueryService.getZoneAggregates(zone, startTime, endTime);

    assertNotNull(aggregates);
    assertEquals(zone, aggregates.getZone());
    assertNull(aggregates.getAverage());
    assertNull(aggregates.getMinimum());
    assertNull(aggregates.getMaximum());
    assertNull(aggregates.getMedian());
    assertEquals(0, aggregates.getDataPointCount());
  }

  private SensorReading createSensorReading(
      String deviceId, String deviceType, String zone, Double value) {
    return new SensorReading(deviceId, deviceType, zone, value, LocalDateTime.now());
  }
}
