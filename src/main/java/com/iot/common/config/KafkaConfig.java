package com.iot.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/** Shared Kafka configuration for both system and simulator services */
@Configuration
@EnableKafka
public class KafkaConfig {}
