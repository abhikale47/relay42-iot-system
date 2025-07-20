package com.iot.system.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SensorAggregateResultTest {

  @Test
  void testConstructorWithAllParameters() {
    Double min = 20.0;
    Double max = 30.0;
    Double average = 25.0;
    Double median = 24.0;
    Integer count = 10;

    SensorAggregateResult result = new SensorAggregateResult(min, max, average, median, null, null, null, count);

    assertEquals(min, result.getMin());
    assertEquals(max, result.getMax());
    assertEquals(average, result.getAverage());
    assertEquals(median, result.getMedian());
    assertEquals(count, result.getCount());
  }

  @Test
  void testConstructorWithThreeParameters() {
    Double min = 15.0;
    Double max = 35.0;
    Double average = 27.5;

    SensorAggregateResult result = new SensorAggregateResult(min, max, average, null, null, null, null, null);

    assertEquals(min, result.getMin());
    assertEquals(max, result.getMax());
    assertEquals(average, result.getAverage());
    assertNull(result.getMedian());
    assertNull(result.getCount());
  }

  @Test
  void testConstructorWithNullValues() {
    SensorAggregateResult result = new SensorAggregateResult(null, null, null, null, null, null, null, 0);

    assertNull(result.getMin());
    assertNull(result.getMax());
    assertNull(result.getAverage());
    assertNull(result.getMedian());
    assertEquals(0, result.getCount());
  }

  @Test
  void testImmutableFields() {
    SensorAggregateResult result = new SensorAggregateResult(10.0, 20.0, 15.0, 12.0, null, null, null, 5);

    // Fields should be immutable (final), so we just test that getters work correctly
    assertEquals(10.0, result.getMin());
    assertEquals(20.0, result.getMax());
    assertEquals(15.0, result.getAverage());
    assertEquals(12.0, result.getMedian());
    assertEquals(5, result.getCount());
  }
}
