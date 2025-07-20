package com.iot.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class SensorAggregateData {
  private String deviceId;
  private String zone;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime startTime;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime endTime;

  private Double average;
  private Double minimum;
  private Double maximum;
  private Double median;
  private Double q1;
  private Double q3;
  private Double p95;

  private Integer dataPointCount;

  public SensorAggregateData() {}

  public SensorAggregateData(
      String deviceId,
      String zone,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Double average,
      Double minimum,
      Double maximum,
      Double median,
      Integer dataPointCount) {
    this.deviceId = deviceId;
    this.zone = zone;
    this.startTime = startTime;
    this.endTime = endTime;
    this.average = average;
    this.minimum = minimum;
    this.maximum = maximum;
    this.median = median;
    this.dataPointCount = dataPointCount;
  }

  public SensorAggregateData(
      String deviceId,
      String zone,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Double average,
      Double minimum,
      Double maximum,
      Double median,
      Double q1,
      Double q3,
      Double p95,
      Integer dataPointCount) {
    this.deviceId = deviceId;
    this.zone = zone;
    this.startTime = startTime;
    this.endTime = endTime;
    this.average = average;
    this.minimum = minimum;
    this.maximum = maximum;
    this.median = median;
    this.q1 = q1;
    this.q3 = q3;
    this.p95 = p95;
    this.dataPointCount = dataPointCount;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public Double getAverage() {
    return average;
  }

  public void setAverage(Double average) {
    this.average = average;
  }

  public Double getMinimum() {
    return minimum;
  }

  public void setMinimum(Double minimum) {
    this.minimum = minimum;
  }

  public Double getMaximum() {
    return maximum;
  }

  public void setMaximum(Double maximum) {
    this.maximum = maximum;
  }

  public Double getMedian() {
    return median;
  }

  public void setMedian(Double median) {
    this.median = median;
  }

  public Double getQ1() {
    return q1;
  }

  public void setQ1(Double q1) {
    this.q1 = q1;
  }

  public Double getQ3() {
    return q3;
  }

  public void setQ3(Double q3) {
    this.q3 = q3;
  }

  public Double getP95() {
    return p95;
  }

  public void setP95(Double p95) {
    this.p95 = p95;
  }



  public Integer getDataPointCount() {
    return dataPointCount;
  }

  public void setDataPointCount(Integer dataPointCount) {
    this.dataPointCount = dataPointCount;
  }

  @Override
  public String toString() {
    return "SensorAggregateData{"
        + "deviceId='"
        + deviceId
        + '\''
        + ", zone='"
        + zone
        + '\''
        + ", startTime="
        + startTime
        + ", endTime="
        + endTime
        + ", average="
        + average
        + ", minimum="
        + minimum
        + ", maximum="
        + maximum
        + ", median="
        + median
        + ", q1="
        + q1
        + ", q3="
        + q3
        + ", p95="
        + p95
        + ", dataPointCount="
        + dataPointCount
        + '}';
  }
}
