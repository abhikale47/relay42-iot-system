package com.iot.simulator.device;

import com.iot.common.model.SensorReading;

public class FuelSensorSimulator extends DeviceSimulator {

  private double currentFuelLevel = 85.0;

  private static final double REFUEL_THRESHOLD = 10.0;

  private static final double FULL_TANK = 100.0;

  private static final double MAX_CONSUMPTION_RATE = 0.1;

  public FuelSensorSimulator(String deviceId, String zone) {
    super(deviceId, "FUEL_SENSOR", zone);
  }

  @Override
  public SensorReading generateReading() {
    double consumption = random.nextDouble() * MAX_CONSUMPTION_RATE;
    currentFuelLevel -= consumption;
    if (currentFuelLevel < REFUEL_THRESHOLD) {
      currentFuelLevel = FULL_TANK;
    }
    currentFuelLevel = Math.max(0.0, Math.min(FULL_TANK, currentFuelLevel));

    return createReading(currentFuelLevel);
  }
}
