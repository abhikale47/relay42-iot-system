package com.iot.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
    exclude = {
      org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
      org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
      org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
      org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
@EnableScheduling
public class IoTSimulatorApplication {

  public static void main(String[] args) {
    System.out.println("Starting IoT Device Simulator...");

    SpringApplication app = new SpringApplication(IoTSimulatorApplication.class);
    app.setAdditionalProfiles("simulator");
    app.run(args);

    System.out.println("IoT Device Simulator started successfully on port 8081!");
  }
}
