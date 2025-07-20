package com.iot.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id", nullable = false)
  @NotNull private String deviceId;

  @Column(name = "device_type", nullable = false)
  @NotNull private String deviceType;

  @Column(name = "zone")
  private String zone;

  @Column(name = "value", nullable = false)
  @NotNull private Double value;

  @Column(name = "timestamp", nullable = false)
  @NotNull private LocalDateTime timestamp;

  public SensorReading() {}

  public SensorReading(
      String deviceId, String deviceType, String zone, Double value, LocalDateTime timestamp) {
    this.deviceId = deviceId;
    this.deviceType = deviceType;
    this.zone = zone;
    this.value = value;
    this.timestamp = timestamp;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    
    SensorReading that = (SensorReading) o;
    
    if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) return false;
    if (deviceType != null ? !deviceType.equals(that.deviceType) : that.deviceType != null) return false;
    if (zone != null ? !zone.equals(that.zone) : that.zone != null) return false;
    if (value != null ? !value.equals(that.value) : that.value != null) return false;
    return timestamp != null ? timestamp.equals(that.timestamp) : that.timestamp == null;
  }

  @Override
  public int hashCode() {
    int result = deviceId != null ? deviceId.hashCode() : 0;
    result = 31 * result + (deviceType != null ? deviceType.hashCode() : 0);
    result = 31 * result + (zone != null ? zone.hashCode() : 0);
    result = 31 * result + (value != null ? value.hashCode() : 0);
    result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SensorReading{"
        + "id="
        + id
        + ", deviceId='"
        + deviceId
        + '\''
        + ", deviceType='"
        + deviceType
        + '\''
        + ", zone='"
        + zone
        + '\''
        + ", value="
        + value
        + ", timestamp="
        + timestamp
        + '}';
  }
}
