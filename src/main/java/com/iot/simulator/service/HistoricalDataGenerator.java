package com.iot.simulator.service;

import com.iot.common.factory.DeviceSimulatorFactory;
import com.iot.common.model.SensorReading;
import com.iot.simulator.device.DeviceSimulator;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class HistoricalDataGenerator {

  private static final Logger logger = LoggerFactory.getLogger(HistoricalDataGenerator.class);

  @Value("${iot.kafka.topic-name}")
  private String kafkaTopic;

  @Autowired private KafkaTemplate<String, SensorReading> kafkaTemplate;

  /**
   * Generate historical data for the last N days
   *
   * @param days Number of days to generate data for
   * @param intervalMinutes Interval between readings in minutes (default: 1 minute)
   * @return CompletableFuture that completes when all data is generated
   */
  public CompletableFuture<Void> generateLastDaysData(int days, int intervalMinutes) {
    logger.info(
        "Starting historical data generation for last {} days with {} minute intervals",
        days,
        intervalMinutes);

    return CompletableFuture.runAsync(
        () -> {
          try {
            List<DeviceSimulator> simulators = DeviceSimulatorFactory.createStandardDeviceSet();
            LocalDateTime endTime = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime startTime = endTime.minusDays(days);

            int totalReadings = 0;
            LocalDateTime currentTime = startTime;

            while (currentTime.isBefore(endTime)) {
              for (DeviceSimulator simulator : simulators) {
                SensorReading reading = generateHistoricalReading(simulator, currentTime);
                sendToKafka(reading);
                totalReadings++;

                // Log progress every 100 readings
                if (totalReadings % 100 == 0) {
                  logger.info(
                      "Generated {} historical readings, current time: {}",
                      totalReadings,
                      currentTime);
                }
              }
              currentTime = currentTime.plusMinutes(intervalMinutes);
            }

            logger.info(
                "Successfully generated {} historical readings for {} devices over {} days",
                totalReadings,
                simulators.size(),
                days);

          } catch (Exception e) {
            logger.error(
                "Error generating historical data for {} days: {}", days, e.getMessage(), e);
            throw new RuntimeException("Failed to generate historical data", e);
          }
        });
  }

  /**
   * Generate historical data for the last N days with default 1-minute intervals
   *
   * @param days Number of days to generate data for
   * @return CompletableFuture that completes when all data is generated
   */
  public CompletableFuture<Void> generateLastDaysData(int days) {
    return generateLastDaysData(days, 1);
  }

  private SensorReading generateHistoricalReading(
      DeviceSimulator simulator, LocalDateTime timestamp) {
    // Generate reading with historical timestamp
    SensorReading reading = simulator.generateReading();

    // Create new reading with historical timestamp
    return new SensorReading(
        reading.getDeviceId(),
        reading.getDeviceType(),
        reading.getZone(),
        addTimeBasedVariation(reading.getValue(), timestamp),
        timestamp);
  }

  /** Add time-based variation to make historical data more realistic */
  private double addTimeBasedVariation(double baseValue, LocalDateTime timestamp) {
    // Add daily and hourly patterns to make data more realistic
    int hour = timestamp.getHour();
    double hourlyVariation = Math.sin(Math.toRadians(hour * 15)) * 0.1; // 15 degrees per hour
    double dailyVariation =
        Math.sin(Math.toRadians(timestamp.getDayOfYear() * 0.986)) * 0.05; // ~365 days
    double randomVariation = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.1;

    return baseValue * (1 + hourlyVariation + dailyVariation + randomVariation);
  }

  private void sendToKafka(SensorReading reading) {
    try {
      kafkaTemplate
          .send(kafkaTopic, reading.getDeviceId(), reading)
          .whenComplete(
              (result, failure) -> {
                if (failure != null) {
                  logger.error(
                      "Failed to send historical reading to Kafka: {}", failure.getMessage());
                }
              });
    } catch (Exception e) {
      logger.error("Error sending historical reading to Kafka: {}", e.getMessage());
    }
  }
}
