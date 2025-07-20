package com.iot.common.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class SensorReadingTest {

  @Test
  void testConstructorAndGetters() {
    String deviceId = "THERMO_001";
    String deviceType = "THERMOSTAT";
    String zone = "ZONE_A";
    Double value = 22.5;
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0);

    SensorReading reading = new SensorReading(deviceId, deviceType, zone, value, timestamp);

    assertEquals(deviceId, reading.getDeviceId());
    assertEquals(deviceType, reading.getDeviceType());
    assertEquals(zone, reading.getZone());
    assertEquals(value, reading.getValue());
    assertEquals(timestamp, reading.getTimestamp());
    // ID is null until persisted to database
  }

  @Test
  void testSetters() {
    SensorReading reading = new SensorReading();

    String deviceId = "HR_001";
    String deviceType = "HEART_RATE";
    String zone = "ZONE_B";
    Double value = 75.0;
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 2, 14, 30);

    reading.setDeviceId(deviceId);
    reading.setDeviceType(deviceType);
    reading.setZone(zone);
    reading.setValue(value);
    reading.setTimestamp(timestamp);

    assertEquals(deviceId, reading.getDeviceId());
    assertEquals(deviceType, reading.getDeviceType());
    assertEquals(zone, reading.getZone());
    assertEquals(value, reading.getValue());
    assertEquals(timestamp, reading.getTimestamp());
  }

  @Test
  void testDefaultConstructor() {
    SensorReading reading = new SensorReading();

    assertNull(reading.getDeviceId());
    assertNull(reading.getDeviceType());
    assertNull(reading.getZone());
    assertNull(reading.getValue());
    assertNull(reading.getTimestamp());
    // ID is null until persisted to database
  }

  @Test
  void testEqualityAndHashCode() {
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0);
    
    SensorReading reading1 = new SensorReading("DEVICE_001", "TYPE_A", "ZONE_A", 10.0, timestamp);
    SensorReading reading2 = new SensorReading("DEVICE_001", "TYPE_A", "ZONE_A", 10.0, timestamp);
    SensorReading reading3 = new SensorReading("DEVICE_002", "TYPE_A", "ZONE_A", 10.0, timestamp);

    assertEquals(reading1, reading2);
    assertEquals(reading1.hashCode(), reading2.hashCode());
    
    assertNotEquals(reading1, reading3);
    assertNotEquals(reading1.hashCode(), reading3.hashCode());
  }

  @Test
  void testToString() {
    SensorReading reading = new SensorReading("TEST_001", "TEST_TYPE", "TEST_ZONE", 42.0, 
        LocalDateTime.of(2024, 1, 1, 12, 0));

    String toString = reading.toString();
    
    assertTrue(toString.contains("TEST_001"));
    assertTrue(toString.contains("TEST_TYPE"));
    assertTrue(toString.contains("TEST_ZONE"));
    assertTrue(toString.contains("42.0"));
  }

  @Test
  void testWithNullValues() {
    SensorReading reading = new SensorReading(null, null, null, null, null);

    assertNull(reading.getDeviceId());
    assertNull(reading.getDeviceType());
    assertNull(reading.getZone());
    assertNull(reading.getValue());
    assertNull(reading.getTimestamp());
  }
}