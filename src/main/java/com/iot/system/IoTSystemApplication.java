package com.iot.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class IoTSystemApplication {
  public static void main(String[] args) {
    System.out.println("Starting IoT Data Processing System...");
    SpringApplication app = new SpringApplication(IoTSystemApplication.class);
    app.setAdditionalProfiles("system");
    app.run(args);
    System.out.println("IoT Data Processing System started successfully on port 8080!");
  }
}
