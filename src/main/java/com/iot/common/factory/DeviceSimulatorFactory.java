package com.iot.common.factory;

import com.iot.simulator.device.DeviceSimulator;
import com.iot.simulator.device.FuelSensorSimulator;
import com.iot.simulator.device.HeartRateSimulator;
import com.iot.simulator.device.ThermostatSimulator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Factory for creating standardized device simulators across the application */
public class DeviceSimulatorFactory {

  private static final Logger logger = LoggerFactory.getLogger(DeviceSimulatorFactory.class);

  /**
   * Create standard set of device simulators with predefined zones
   *
   * @return List of configured device simulators
   */
  public static List<DeviceSimulator> createStandardDeviceSet() {
    List<DeviceSimulator> simulators = new ArrayList<>();

    // Create thermostats
    List<String> thermoZones = Arrays.asList("living-room", "bedroom", "kitchen");
    for (int i = 0; i <= 1; i++) {
      String zone = thermoZones.get((i) % thermoZones.size());
      simulators.add(new ThermostatSimulator("THERMO-" + i + 1, zone));
    }

    // Create heart rate monitors
    List<String> hrZones = Arrays.asList("gym", "home", "office");
    for (int i = 0; i <= 1; i++) {
      String zone = hrZones.get((i) % hrZones.size());
      simulators.add(new HeartRateSimulator("HR-" + i + 1, zone));
    }

    // Create fuel sensors
    List<String> fuelZones = Arrays.asList("garage", "parking-lot");
    for (int i = 0; i <= 0; i++) {
      String zone = fuelZones.get((i) % fuelZones.size());
      simulators.add(new FuelSensorSimulator("FUEL-" + i + 1, zone));
    }

    logger.debug("Created {} standard device simulators", simulators.size());
    return simulators;
  }
}
