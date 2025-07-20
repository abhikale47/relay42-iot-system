package com.iot.system.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("system")
@EntityScan(basePackages = {"com.iot.common.model", "com.iot.system.model"})
@EnableJpaRepositories(basePackages = "com.iot.system.repository")
public class JpaConfig {
  // This configuration ensures JPA is only active for system profile
}