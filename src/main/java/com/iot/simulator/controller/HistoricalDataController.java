package com.iot.simulator.controller;

import com.iot.simulator.service.HistoricalDataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Data Simulator",
    description = "Historical IoT data generation for testing and development")
@RestController
@RequestMapping("/api/simulator/historical")
public class HistoricalDataController {

  private static final Logger logger = LoggerFactory.getLogger(HistoricalDataController.class);

  @Autowired private HistoricalDataGenerator historicalDataGenerator;

  /** Generate historical data for the last N days with default 1-minute intervals */
  @Operation(
      summary = "Generate last N days of historical data",
      description = "Generates multiple days of historical IoT sensor data with 1-minute intervals")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Data generation started successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid number of days (must be 1-30)"),
        @ApiResponse(responseCode = "500", description = "Failed to start data generation")
      })
  @GetMapping("/generate-last-days/{days}")
  public ResponseEntity<Map<String, Object>> generateLastDaysData(
      @Parameter(description = "Number of days (1-30)", example = "5") @PathVariable int days) {

    logger.info("Received request to generate historical data for last {} days", days);

    if (days < 1 || days > 30) {
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "status", "error",
                  "message", "Number of days must be between 1 and 30"));
    }

    try {
      CompletableFuture<Void> future = historicalDataGenerator.generateLastDaysData(days);

      future.whenComplete(
          (result, throwable) -> {
            if (throwable != null) {
              logger.error("Historical data generation failed: {}", throwable.getMessage());
            } else {
              logger.info("Historical data generation completed successfully");
            }
          });

      int estimatedReadings = 1440 * days * 5; // 1440 minutes per day * days * 5 devices

      return ResponseEntity.ok(
          Map.of(
              "status",
              "started",
              "message",
              String.format(
                  "Historical data generation started for last %d days with 1-minute intervals",
                  days),
              "estimated_readings",
              estimatedReadings,
              "interval_minutes",
              1,
              "days",
              days));

    } catch (Exception e) {
      logger.error("Error starting historical data generation: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body(
              Map.of(
                  "status",
                  "error",
                  "message",
                  "Failed to start historical data generation: " + e.getMessage()));
    }
  }

  /** Generate historical data for the last N days with custom interval */
  @Operation(
      summary = "Generate last N days with custom interval",
      description =
          "Generates multiple days of historical data with custom interval (1-60 minutes)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Data generation started successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters"),
        @ApiResponse(responseCode = "500", description = "Failed to start data generation")
      })
  @GetMapping("/generate-last-days/{days}/{intervalMinutes}")
  public ResponseEntity<Map<String, Object>> generateLastDaysDataWithInterval(
      @Parameter(description = "Number of days (1-30)", example = "5") @PathVariable int days,
      @Parameter(description = "Interval in minutes (1-60)", example = "5") @PathVariable
          int intervalMinutes) {

    logger.info(
        "Received request to generate historical data for last {} days with {} minute intervals",
        days,
        intervalMinutes);

    if (days < 1 || days > 30) {
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "status", "error",
                  "message", "Number of days must be between 1 and 30"));
    }

    if (intervalMinutes < 1 || intervalMinutes > 60) {
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "status", "error",
                  "message", "Interval must be between 1 and 60 minutes"));
    }

    try {
      CompletableFuture<Void> future =
          historicalDataGenerator.generateLastDaysData(days, intervalMinutes);

      future.whenComplete(
          (result, throwable) -> {
            if (throwable != null) {
              logger.error("Historical data generation failed: {}", throwable.getMessage());
            } else {
              logger.info("Historical data generation completed successfully");
            }
          });

      int estimatedReadings =
          (1440 * days / intervalMinutes) * 5; // Total minutes / interval * 5 devices

      return ResponseEntity.ok(
          Map.of(
              "status", "started",
              "message",
                  String.format(
                      "Historical data generation started for last %d days with %d-minute intervals",
                      days, intervalMinutes),
              "estimated_readings", estimatedReadings,
              "interval_minutes", intervalMinutes,
              "days", days));

    } catch (Exception e) {
      logger.error("Error starting historical data generation: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body(
              Map.of(
                  "status",
                  "error",
                  "message",
                  "Failed to start historical data generation: " + e.getMessage()));
    }
  }
}
