package com.iot.system.service;

import com.iot.common.model.SensorReading;
import com.iot.system.repository.SensorReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorDataProcessingService {
  private static final Logger log = LoggerFactory.getLogger(SensorDataProcessingService.class);

  @Autowired private SensorReadingRepository sensorReadingRepository;

  @RetryableTopic(
      attempts = "3",
      backoff = @Backoff(delay = 1000, multiplier = 2.0),
      autoCreateTopics = "true",
      dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR,
      retryTopicSuffix = "-retry",
      dltTopicSuffix = "-dlt")
  @KafkaListener(topics = "${iot.kafka.topic-name}", groupId = "iot-processing-group-v2")
  public void processSensorReadingBatch(List<SensorReading> readings) {
    if (readings.isEmpty()) {
      return;
    }
    
    // Filter valid readings
    List<SensorReading> validReadings = readings.stream()
        .filter(this::isValidReading)
        .toList();
    
    int invalidCount = readings.size() - validReadings.size();
    if (invalidCount > 0) {
      log.warn("Discarded {} invalid readings from batch of {}", invalidCount, readings.size());
    }
    
    if (!validReadings.isEmpty()) {
      // Bulk insert all valid readings
      sensorReadingRepository.saveAll(validReadings);
      log.debug("Processed batch of {} sensor readings", validReadings.size());
    }
    
    // Offset automatically committed after method completes successfully
  }

  @DltHandler
  public void handleDltReading(
      SensorReading reading,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
    log.error(
        "Message sent to DLT - Topic: {}, Reading: {}, Error: {}",
        topic,
        reading,
        exceptionMessage);
  }

  private boolean isValidReading(SensorReading reading) {
    return reading != null
        && reading.getDeviceId() != null
        && reading.getDeviceType() != null
        && reading.getValue() != null
        && reading.getTimestamp() != null;
  }
}
