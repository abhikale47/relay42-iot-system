package com.iot.simulator.device;

import com.iot.common.model.SensorReading;

public class HeartRateSimulator extends DeviceSimulator {

  private int currentHeartRate = 70;

  private static final int MIN_HEART_RATE = 60;

  private static final int MAX_HEART_RATE = 180;

  private static final int MAX_CHANGE = 2;

  public HeartRateSimulator(String deviceId, String zone) {
    super(deviceId, "HEART_RATE", zone);
  }

  @Override
  public SensorReading generateReading() {
    int magnitude = random.nextInt(MAX_CHANGE + 1); // [0, MAX_CHANGE]
    int direction = random.nextBoolean() ? 1 : -1;
    int change = magnitude * direction;
    currentHeartRate += change;
    currentHeartRate = Math.max(MIN_HEART_RATE, Math.min(MAX_HEART_RATE, currentHeartRate));

    return createReading(currentHeartRate);
  }
}
