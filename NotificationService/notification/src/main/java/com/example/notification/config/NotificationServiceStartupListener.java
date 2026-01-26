package com.example.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class NotificationServiceStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${notification.kafka.topic:auth_notifications}")
    private String notificationTopic;

    @Value("${spring.kafka.consumer.group-id:notification-service-group}")
    private String consumerGroupId;


    private static final List<String> EVENT_TYPES = Arrays.asList(
            "PASSWORD_RESET_REQUESTED",
            "ACCOUNT_LOCKED",
            "USER_REGISTERED",
            "PASSWORD_CHANGED"
    );

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("=".repeat(80));
        log.info("NotificationService Startup Information");
        log.info("=".repeat(80));
        log.info("Kafka Bootstrap Servers: {}", bootstrapServers);
        log.info("Consumer Group ID: {}", consumerGroupId);
        log.info("");
        log.info("Listening to Kafka Topic:");
        log.info("  - Topic: {}", notificationTopic);
        log.info("");
        log.info("Event Types Consumed:");
        EVENT_TYPES.forEach(eventType -> log.info("  - {}", eventType));
        log.info("");
        log.info("Event Descriptions:");
        log.info("  - PASSWORD_RESET_REQUESTED: User requests password reset (forgot password)");
        log.info("  - ACCOUNT_LOCKED: User account has been locked due to security reasons");
        log.info("  - USER_REGISTERED: New user has registered");
        log.info("  - PASSWORD_CHANGED: User has successfully changed their password");
        log.info("=".repeat(80));
    }
}
