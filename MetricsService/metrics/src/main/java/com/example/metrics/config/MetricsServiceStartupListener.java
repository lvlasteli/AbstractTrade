package com.example.metrics.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MetricsServiceStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${metrics.kafka.topic:auth_metrics}")
    private String metricsTopic;

    @Value("${spring.kafka.consumer.group-id:metrics-service-group}")
    private String consumerGroupId;

    private static final List<String> EVENT_TYPES = Arrays.asList(
            "USER_LOGGED_IN",
            "USER_LOGGED_OUT",
            "USER_REGISTERED",
            "ACCOUNT_LOCKED",
            "IP_BLOCKED"
    );

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=".repeat(80));
        log.info("MetricsService Startup Information");
        log.info("=".repeat(80));
        log.info("Kafka Bootstrap Servers: {}", bootstrapServers);
        log.info("Consumer Group ID: {}", consumerGroupId);
        log.info("");
        log.info("Listening to Kafka Topic:");
        log.info("  - Topic: {}", metricsTopic);
        log.info("");
        log.info("Event Types Consumed:");
        EVENT_TYPES.forEach(eventType -> log.info("  - {}", eventType));
        log.info("");
        log.info("Event Descriptions:");
        log.info("  - USER_LOGGED_IN: Track successful authentication events, login frequency, and user activity patterns");
        log.info("  - USER_LOGGED_OUT: Monitor session termination and user logout patterns");
        log.info("  - USER_REGISTERED: Track user growth, registration trends, and new user acquisition metrics");
        log.info("  - ACCOUNT_LOCKED: Security metrics for account lockouts, failed login attempts, and threat detection");
        log.info("  - IP_BLOCKED: Track IP-based blocking events from GatewayService, including rate limit violations and suspicious activity patterns");
        log.info("=".repeat(80));
    }
}
