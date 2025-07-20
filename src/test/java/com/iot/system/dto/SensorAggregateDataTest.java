package com.iot.system.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SensorAggregateDataTest {

  @Test
  void testConstructorAndGetters() {
    String deviceId = "THERMO_001";
    String zone = "ZONE_A";
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    Double average = 22.5;
    Double minimum = 20.0;
    Double maximum = 25.0;
    Double median = 22.0;
    Integer dataPointCount = 100;

    SensorAggregateData data =
        new SensorAggregateData(
            deviceId,
            zone,
            startTime,
            endTime,
            average,
            minimum,
            maximum,
            median,
            dataPointCount);

    assertEquals(deviceId, data.getDeviceId());
    assertEquals(zone, data.getZone());
    assertEquals(startTime, data.getStartTime());
    assertEquals(endTime, data.getEndTime());
    assertEquals(average, data.getAverage());
    assertEquals(minimum, data.getMinimum());
    assertEquals(maximum, data.getMaximum());
    assertEquals(median, data.getMedian());
    assertEquals(dataPointCount, data.getDataPointCount());
  }

  @Test
  void testDeviceSpecificConstructor() {
    String deviceId = "THERMO_001";
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);

    SensorAggregateData data =
        new SensorAggregateData(
            deviceId, null, startTime, endTime, 22.5, 20.0, 25.0, 22.0, 50);

    assertEquals(deviceId, data.getDeviceId());
    assertNull(data.getZone());
    assertEquals(startTime, data.getStartTime());
    assertEquals(endTime, data.getEndTime());
    assertEquals(22.5, data.getAverage());
    assertEquals(20.0, data.getMinimum());
    assertEquals(25.0, data.getMaximum());
    assertEquals(22.0, data.getMedian());
    assertEquals(50, data.getDataPointCount());
  }

  @Test
  void testZoneSpecificConstructor() {
    String zone = "ZONE_A";
    LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 11, 0);

    SensorAggregateData data =
        new SensorAggregateData(null, zone, startTime, endTime, 30.0, 25.0, 35.0, 29.0, 75);

    assertNull(data.getDeviceId());
    assertEquals(zone, data.getZone());
    assertEquals(startTime, data.getStartTime());
    assertEquals(endTime, data.getEndTime());
    assertEquals(30.0, data.getAverage());
    assertEquals(25.0, data.getMinimum());
    assertEquals(35.0, data.getMaximum());
    assertEquals(29.0, data.getMedian());
    assertEquals(75, data.getDataPointCount());
  }

  @Test
  void testWithNullValues() {
    SensorAggregateData data =
        new SensorAggregateData(
            "DEVICE_001",
            null,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null,
            null,
            null,
            0);

    assertEquals("DEVICE_001", data.getDeviceId());
    assertNull(data.getZone());
    assertNull(data.getAverage());
    assertNull(data.getMinimum());
    assertNull(data.getMaximum());
    assertNull(data.getMedian());
    assertEquals(0, data.getDataPointCount());
  }

  @Test
  void testSetters() {
    SensorAggregateData data =
        new SensorAggregateData(
            "DEVICE_001",
            "ZONE_A",
            LocalDateTime.now(),
            LocalDateTime.now(),
            20.0,
            15.0,
            25.0,
            19.0,
            10);

    // Test setters
    data.setDeviceId("NEW_DEVICE");
    data.setZone("NEW_ZONE");
    data.setAverage(30.0);
    data.setMinimum(25.0);
    data.setMaximum(35.0);
    data.setMedian(29.0);
    data.setDataPointCount(20);

    assertEquals("NEW_DEVICE", data.getDeviceId());
    assertEquals("NEW_ZONE", data.getZone());
    assertEquals(30.0, data.getAverage());
    assertEquals(25.0, data.getMinimum());
    assertEquals(35.0, data.getMaximum());
    assertEquals(29.0, data.getMedian());
    assertEquals(20, data.getDataPointCount());
  }
}
