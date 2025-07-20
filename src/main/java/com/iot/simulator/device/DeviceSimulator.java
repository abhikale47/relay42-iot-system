package com.iot.simulator.device;

import com.iot.common.model.SensorReading;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

public abstract class DeviceSimulator {

  protected final String deviceId;

  protected final String deviceType;

  protected final String zone;

  protected final Random random = new Random();

  public DeviceSimulator(String deviceId, String deviceType, String zone) {
    this.deviceId = deviceId;
    this.deviceType = deviceType;
    this.zone = zone;
  }

  public abstract SensorReading generateReading();

  protected SensorReading createReading(double value) {
    return new SensorReading(deviceId, deviceType, zone, value, LocalDateTime.now(ZoneOffset.UTC));
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public String getZone() {
    return zone;
  }
}
