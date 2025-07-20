package com.iot.system.service;

import com.iot.common.model.SensorReading;
import com.iot.system.dto.SensorAggregateData;
import com.iot.system.repository.SensorAggregateResult;
import com.iot.system.repository.SensorReadingRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("system")
public class SensorQueryService {

  @Autowired private SensorReadingRepository sensorReadingRepository;

  public List<SensorReading> getDeviceReadings(
      String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
    return sensorReadingRepository.findByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
  }

  public List<SensorReading> getZoneReadings(
      String zone, LocalDateTime startTime, LocalDateTime endTime) {
    return sensorReadingRepository.findByZoneAndTimestampBetween(zone, startTime, endTime);
  }

  public List<SensorReading> getDeviceTypeReadings(
      String deviceType, LocalDateTime startTime, LocalDateTime endTime) {
    return sensorReadingRepository.findByDeviceTypeAndTimestampBetween(
        deviceType, startTime, endTime);
  }

  public SensorAggregateData getDeviceAggregates(
      String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
    SensorAggregateResult result =
        sensorReadingRepository.findAggregatesByDeviceIdAndTimestampBetween(
            deviceId, startTime, endTime);

    return new SensorAggregateData(
        deviceId,
        null,
        startTime,
        endTime,
        result.getAverage(),
        result.getMin(),
        result.getMax(),
        result.getMedian(),
        result.getQ1(),
        result.getQ3(),
        result.getP95(),
        result.getCount());
  }

  public SensorAggregateData getZoneAggregates(
      String zone, LocalDateTime startTime, LocalDateTime endTime) {
    SensorAggregateResult result =
        sensorReadingRepository.findAggregatesByZoneAndTimestampBetween(zone, startTime, endTime);

    return new SensorAggregateData(
        null,
        zone,
        startTime,
        endTime,
        result.getAverage(),
        result.getMin(),
        result.getMax(),
        result.getMedian(),
        result.getQ1(),
        result.getQ3(),
        result.getP95(),
        result.getCount());
  }
}
