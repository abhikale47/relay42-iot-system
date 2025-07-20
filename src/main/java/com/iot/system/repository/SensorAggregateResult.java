package com.iot.system.repository;

public class SensorAggregateResult {
  private final Double min;
  private final Double max;
  private final Double average;
  private final Double median;
  private final Double q1;
  private final Double q3;
  private final Double p95;
  private final Integer count;

  public SensorAggregateResult(
      Double min,
      Double max,
      Double average,
      Double median,
      Double q1,
      Double q3,
      Double p95,
      Integer count) {
    this.min = min;
    this.max = max;
    this.average = average;
    this.median = median;
    this.q1 = q1;
    this.q3 = q3;
    this.p95 = p95;
    this.count = count;
  }

  public Double getMin() {
    return min;
  }

  public Double getMax() {
    return max;
  }

  public Double getAverage() {
    return average;
  }

  public Double getMedian() {
    return median;
  }

  public Double getQ1() {
    return q1;
  }

  public Double getQ3() {
    return q3;
  }

  public Double getP95() {
    return p95;
  }


  public Integer getCount() {
    return count;
  }
}
