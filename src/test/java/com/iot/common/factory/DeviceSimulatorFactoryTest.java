package com.iot.common.factory;

import static org.junit.jupiter.api.Assertions.*;

import com.iot.simulator.device.DeviceSimulator;
import com.iot.simulator.device.FuelSensorSimulator;
import com.iot.simulator.device.HeartRateSimulator;
import com.iot.simulator.device.ThermostatSimulator;
import org.junit.jupiter.api.Test;

import java.util.List;

class DeviceSimulatorFactoryTest {

  @Test
  void testCreateStandardDeviceSet() {
    List<DeviceSimulator> simulators = DeviceSimulatorFactory.createStandardDeviceSet();
    
    assertNotNull(simulators);
    assertFalse(simulators.isEmpty());
    
    // Verify expected number of simulators (2 thermostats + 2 heart rate + 1 fuel = 5)
    assertEquals(5, simulators.size());
  }

  @Test
  void testCreateStandardDeviceSetContainsCorrectTypes() {
    List<DeviceSimulator> simulators = DeviceSimulatorFactory.createStandardDeviceSet();
    
    long thermostatCount = simulators.stream()
        .filter(s -> s instanceof ThermostatSimulator)
        .count();
    long heartRateCount = simulators.stream()
        .filter(s -> s instanceof HeartRateSimulator)
        .count();
    long fuelSensorCount = simulators.stream()
        .filter(s -> s instanceof FuelSensorSimulator)
        .count();
    
    assertEquals(2, thermostatCount, "Should have 2 thermostats");
    assertEquals(2, heartRateCount, "Should have 2 heart rate monitors");
    assertEquals(1, fuelSensorCount, "Should have 1 fuel sensor");
  }

  @Test
  void testCreateStandardDeviceSetHasUniqueDeviceIds() {
    List<DeviceSimulator> simulators = DeviceSimulatorFactory.createStandardDeviceSet();
    
    long uniqueDeviceIdCount = simulators.stream()
        .map(DeviceSimulator::getDeviceId)
        .distinct()
        .count();
    
    assertEquals(simulators.size(), uniqueDeviceIdCount, "All device IDs should be unique");
  }

  @Test
  void testCreateStandardDeviceSetHasValidZones() {
    List<DeviceSimulator> simulators = DeviceSimulatorFactory.createStandardDeviceSet();
    
    for (DeviceSimulator simulator : simulators) {
      assertNotNull(simulator.getZone(), "Zone should not be null");
      assertFalse(simulator.getZone().isEmpty(), "Zone should not be empty");
    }
  }

  @Test
  void testCreateStandardDeviceSetHasValidDeviceTypes() {
    List<DeviceSimulator> simulators = DeviceSimulatorFactory.createStandardDeviceSet();
    
    for (DeviceSimulator simulator : simulators) {
      assertNotNull(simulator.getDeviceType(), "Device type should not be null");
      assertFalse(simulator.getDeviceType().isEmpty(), "Device type should not be empty");
      
      // Verify device types match expected values
      assertTrue(
          simulator.getDeviceType().equals("THERMOSTAT") ||
          simulator.getDeviceType().equals("HEART_RATE") ||
          simulator.getDeviceType().equals("FUEL_SENSOR"),
          "Device type should be one of the expected types: " + simulator.getDeviceType());
    }
  }

  @Test
  void testCreateStandardDeviceSetGeneratesReadings() {
    List<DeviceSimulator> simulators = DeviceSimulatorFactory.createStandardDeviceSet();
    
    for (DeviceSimulator simulator : simulators) {
      assertDoesNotThrow(() -> {
        var reading = simulator.generateReading();
        assertNotNull(reading, "Generated reading should not be null");
        assertNotNull(reading.getValue(), "Reading value should not be null");
        assertNotNull(reading.getDeviceId(), "Reading device ID should not be null");
        assertNotNull(reading.getDeviceType(), "Reading device type should not be null");
      }, "Should be able to generate readings without exceptions");
    }
  }
}