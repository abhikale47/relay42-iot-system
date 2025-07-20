package com.iot.simulator.device;

import com.iot.common.model.SensorReading;

public class ThermostatSimulator extends DeviceSimulator {

  private double currentTemperature = 22.0;

  private static final double MIN_TEMPERATURE = 18.0;

  private static final double MAX_TEMPERATURE = 30.0;

  private static final double TEMPERATURE_VARIATION = 0.5;

  public ThermostatSimulator(String deviceId, String zone) {
    super(deviceId, "THERMOSTAT", zone);
  }

  @Override
  public SensorReading generateReading() {
    double magnitude = random.nextDouble(TEMPERATURE_VARIATION);
    double direction = random.nextBoolean() ? 1 : -1;
    double change = magnitude * direction;
    currentTemperature += change;
    currentTemperature = Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, currentTemperature));

    return createReading(currentTemperature);
  }
}
