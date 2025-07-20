package com.iot.system.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iot.common.model.SensorReading;
import com.iot.system.repository.SensorReadingRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensorDataProcessingServiceTest {

  @Mock private SensorReadingRepository sensorReadingRepository;

  @InjectMocks private SensorDataProcessingService sensorDataProcessingService;

  private SensorReading validReading;
  private SensorReading invalidReading;

  @BeforeEach
  void setUp() {
    validReading =
        new SensorReading("THERMO_001", "THERMOSTAT", "ZONE_A", 22.5, LocalDateTime.now());

    invalidReading = new SensorReading();
  }

  @Test
  void testProcessValidSensorReadingBatch() {
    List<SensorReading> batch = Arrays.asList(validReading);
    
    sensorDataProcessingService.processSensorReadingBatch(batch);

    verify(sensorReadingRepository, times(1)).saveAll(batch);
  }

  @Test
  void testProcessBatchWithInvalidReadings() {
    List<SensorReading> batch = Arrays.asList(validReading, invalidReading);
    
    sensorDataProcessingService.processSensorReadingBatch(batch);

    // Should only save valid readings
    verify(sensorReadingRepository, times(1)).saveAll(Arrays.asList(validReading));
  }

  @Test
  void testProcessEmptyBatch() {
    List<SensorReading> emptyBatch = Arrays.asList();
    
    sensorDataProcessingService.processSensorReadingBatch(emptyBatch);

    verify(sensorReadingRepository, never()).saveAll(any());
  }

  @Test
  void testProcessBatchWithRepositoryException() {
    List<SensorReading> batch = Arrays.asList(validReading);
    when(sensorReadingRepository.saveAll(any()))
        .thenThrow(new RuntimeException("Database error"));

    assertThrows(
        RuntimeException.class,
        () -> {
          sensorDataProcessingService.processSensorReadingBatch(batch);
        });

    verify(sensorReadingRepository, times(1)).saveAll(batch);
  }
}
