package com.iot.system.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ViewRefreshService {

  private static final Logger logger = LoggerFactory.getLogger(ViewRefreshService.class);
  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Autowired private JdbcTemplate jdbcTemplate;

  /** Refresh all continuous aggregates (materialized views) */
  public CompletableFuture<Map<String, Object>> refreshAllViews() {
    logger.info("Starting refresh of all continuous aggregates");

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            long startTime = System.currentTimeMillis();

            // Refresh device hourly aggregates
            jdbcTemplate.execute(
                "CALL refresh_continuous_aggregate('device_hourly_aggregates', NULL, NULL)");
            logger.info("Refreshed device_hourly_aggregates");

            // Refresh zone hourly aggregates
            jdbcTemplate.execute(
                "CALL refresh_continuous_aggregate('zone_hourly_aggregates', NULL, NULL)");
            logger.info("Refreshed zone_hourly_aggregates");

            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully refreshed all continuous aggregates in {} ms", duration);

            return Map.of(
                "status",
                "success",
                "message",
                "All continuous aggregates refreshed successfully",
                "refreshed_views",
                new String[] {"device_hourly_aggregates", "zone_hourly_aggregates"},
                "duration_ms",
                duration,
                "timestamp",
                LocalDateTime.now().format(TIMESTAMP_FORMAT));

          } catch (Exception e) {
            logger.error("Error refreshing continuous aggregates: {}", e.getMessage(), e);
            return Map.of(
                "status",
                "error",
                "message",
                "Failed to refresh continuous aggregates: " + e.getMessage(),
                "timestamp",
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
          }
        });
  }
}
