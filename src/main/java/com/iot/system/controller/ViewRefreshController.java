package com.iot.system.controller;

import com.iot.system.service.ViewRefreshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Database Views",
    description = "Manual refresh of TimescaleDB continuous aggregates and materialized views")
@RestController
@RequestMapping("/api/system/views")
@PreAuthorize("hasRole('ADMIN')")
public class ViewRefreshController {

  private static final Logger logger = LoggerFactory.getLogger(ViewRefreshController.class);

  @Autowired private ViewRefreshService viewRefreshService;

  /** Refresh all continuous aggregates */
  @Operation(
      summary = "Refresh all continuous aggregates",
      description =
          "Manually refresh all continuous aggregates (device_hourly_aggregates, zone_hourly_aggregates)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Refresh operation started successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Failed to start refresh operation")
      })
  @SecurityRequirement(name = "bearer-jwt")
  @PostMapping("/refresh")
  public ResponseEntity<Map<String, Object>> refreshAllViews() {
    logger.info("Received request to refresh all continuous aggregates");

    try {
      CompletableFuture<Map<String, Object>> future = viewRefreshService.refreshAllViews();

      // Start async operation and return immediate response
      future.whenComplete(
          (result, throwable) -> {
            if (throwable != null) {
              logger.error("Failed to refresh all views: {}", throwable.getMessage());
            } else {
              logger.info("All views refresh completed: {}", result.get("status"));
            }
          });

      return ResponseEntity.ok(
          Map.of(
              "status", "started",
              "message", "Refresh operation for all continuous aggregates has been started",
              "operation", "refresh_all_views",
              "estimated_duration", "30-60 seconds depending on data volume"));

    } catch (Exception e) {
      logger.error("Error starting refresh operation: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body(
              Map.of(
                  "status",
                  "error",
                  "message",
                  "Failed to start refresh operation: " + e.getMessage()));
    }
  }
}
