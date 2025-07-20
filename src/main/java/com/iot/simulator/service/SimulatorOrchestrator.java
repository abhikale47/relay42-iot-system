package com.iot.simulator.service;

import com.iot.common.factory.DeviceSimulatorFactory;
import com.iot.common.model.SensorReading;
import com.iot.simulator.device.DeviceSimulator;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimulatorOrchestrator {

  private static final Logger logger = LoggerFactory.getLogger(SimulatorOrchestrator.class);

  @Value("${iot.kafka.topic-name}")
  private String kafkaTopic;

  @Autowired private KafkaTemplate<String, SensorReading> kafkaTemplate;

  private final List<DeviceSimulator> simulators;
  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

  @PostConstruct
  public void initializeSimulators() {
    logger.info("Initializing IoT device simulators...");

    for (DeviceSimulator simulator : simulators) {
      scheduleSimulator(simulator, 1000);
    }

    logger.info("Created {} total device simulators", simulators.size());
    logger.info("Starting IoT device simulation with {} devices", simulators.size());
  }

  public SimulatorOrchestrator() {
    this.simulators = DeviceSimulatorFactory.createStandardDeviceSet();
  }

  private void scheduleSimulator(DeviceSimulator simulator, long intervalMs) {
    executorService.scheduleAtFixedRate(
        () -> {
          try {
            SensorReading reading = simulator.generateReading();
            sendToKafka(reading);

            logger.debug(
                "Generated reading: {} from device: {} in zone: {} with value: {}",
                reading.getDeviceType(),
                reading.getDeviceId(),
                reading.getZone(),
                reading.getValue());

          } catch (Exception e) {
            logger.error(
                "Error generating reading for device {}: {}",
                simulator.getDeviceId(),
                e.getMessage());
          }
        },
        1000,
        intervalMs,
        TimeUnit.MILLISECONDS);
  }

  private void sendToKafka(SensorReading reading) {
    try {
      kafkaTemplate
          .send(kafkaTopic, reading.getDeviceId(), reading)
          .whenComplete(
              (result, failure) -> {
                if (failure != null) {
                  logger.error("Failed to send reading to Kafka: {}", failure.getMessage());
                }
              });
    } catch (Exception e) {
      logger.error("Error sending to Kafka: {}", e.getMessage());
    }
  }
}
