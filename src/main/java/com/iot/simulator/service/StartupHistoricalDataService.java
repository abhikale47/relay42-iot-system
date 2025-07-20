package com.iot.simulator.service;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StartupHistoricalDataService {

  private static final Logger logger = LoggerFactory.getLogger(StartupHistoricalDataService.class);

  @Autowired private HistoricalDataGenerator historicalDataGenerator;

  @EventListener(ApplicationReadyEvent.class)
  public void handleApplicationReady() {
    String generateLastDay = System.getProperty("simulator.generate-last-day");
    String historicalOnly = System.getProperty("simulator.historical-only");
    String intervalMinutesStr = System.getProperty("simulator.interval-minutes");

    if ("true".equals(generateLastDay)) {
      int intervalMinutes = 1;
      try {
        intervalMinutes = Integer.parseInt(intervalMinutesStr);
      } catch (NumberFormatException e) {
        logger.warn("Invalid interval minutes, using default: 1");
      }

      logger.info(
          "Starting historical data generation for last 1 day with {} minute intervals",
          intervalMinutes);

      CompletableFuture<Void> future =
          historicalDataGenerator.generateLastDaysData(1, intervalMinutes);

      if ("true".equals(historicalOnly)) {
        // Wait for completion and then exit
        future.whenComplete(
            (result, throwable) -> {
              if (throwable != null) {
                logger.error("Historical data generation failed: {}", throwable.getMessage());
                System.exit(1);
              } else {
                logger.info("Historical data generation completed successfully. Exiting...");
                System.exit(0);
              }
            });
      } else {
        // Log completion but continue with live simulation
        future.whenComplete(
            (result, throwable) -> {
              if (throwable != null) {
                logger.error("Historical data generation failed: {}", throwable.getMessage());
              } else {
                logger.info(
                    "Historical data generation completed successfully. Continuing with live simulation...");
              }
            });
      }
    }
  }
}
