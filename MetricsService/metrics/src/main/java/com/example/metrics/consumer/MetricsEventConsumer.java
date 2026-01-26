package com.example.metrics.consumer;

import com.example.shared.events.constants.KafkaConstants;
import com.example.shared.events.schema.AccountLockedEvent;
import com.example.shared.events.schema.AuthEvent;
import com.example.shared.events.schema.AuthenticationFailedEvent;
import com.example.shared.events.schema.UserLoggedInEvent;
import com.example.shared.events.schema.UserLoggedOutEvent;
import com.example.shared.events.schema.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer for metric events from auth_metrics Kafka topic.
 * Consumes AuthEvent instances and processes them for metrics collection.
 * Currently logs received messages. Future implementation will store metrics in time-series database.
 */
@Component
@Slf4j
public class MetricsEventConsumer {

    @KafkaListener(
            topics = "${metrics.kafka.topic:auth_metrics}",
            groupId = "${spring.kafka.consumer.group-id:metrics-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMetricEvent(
            @Payload AuthEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            log.info("=".repeat(80));
            log.info("Received metric event:");
            log.info("  Topic: {}", topic);
            log.info("  Partition: {}", partition);
            log.info("  Offset: {}", offset);
            log.info("  Key: {}", key);
            log.info("  Event Type: {}", event != null ? event.getEventType() : "null");
            log.info("  Event ID: {}", event != null ? event.getEventId() : "null");
            log.info("  Timestamp: {}", event != null ? event.getTimestamp() : "null");
            log.info("=".repeat(80));

            if (event == null) {
                log.warn("Received null event, skipping processing");
                return;
            }

            // Process event based on type
            String eventType = event.getEventType();
            switch (eventType) {
                case KafkaConstants.USER_LOGGED_IN:
                    processUserLoggedIn((UserLoggedInEvent) event);
                    break;
                case KafkaConstants.USER_LOGGED_OUT:
                    processUserLoggedOut((UserLoggedOutEvent) event);
                    break;
                case KafkaConstants.USER_REGISTERED:
                    processUserRegistered((UserRegisteredEvent) event);
                    break;
                case KafkaConstants.ACCOUNT_LOCKED:
                    processAccountLocked((AccountLockedEvent) event);
                    break;
                case KafkaConstants.AUTHENTICATION_FAILED:
                    processAuthenticationFailed((AuthenticationFailedEvent) event);
                    break;
                default:
                    log.warn("Unknown event type: {}, event: {}", eventType, event);
            }

            // TODO: Implement time-series database integration (InfluxDB/TimescaleDB)
            // TODO: Store metrics in time-series database
            // TODO: Implement metric aggregation logic
            // TODO: Add error handling and retry logic

        } catch (Exception e) {
            log.error("Error processing metric event from topic: {}, key: {}", topic, key, e);
            throw e; // Re-throw to trigger error handler
        }
    }

    private void processUserLoggedIn(UserLoggedInEvent event) {
        log.info("Processing USER_LOGGED_IN event - UserId: {}, Email: {}, IP: {}, Device: {}",
                event.getUserId(), event.getEmail(), event.getIpAddress(), event.getDeviceInfo());
        // TODO: Store login metrics (login count, IP tracking, device tracking, etc.)
    }

    private void processUserLoggedOut(UserLoggedOutEvent event) {
        log.info("Processing USER_LOGGED_OUT event - UserId: {}, LogoutType: {}",
                event.getUserId(), event.getLogoutType());
        // TODO: Store logout metrics (session duration, logout type distribution, etc.)
    }

    private void processUserRegistered(UserRegisteredEvent event) {
        log.info("Processing USER_REGISTERED event - UserId: {}, Email: {}, Username: {}, Roles: {}",
                event.getUserId(), event.getEmail(), event.getUsername(), event.getRoles());
        // TODO: Store registration metrics (user growth, registration trends, role distribution, etc.)
    }

    private void processAccountLocked(AccountLockedEvent event) {
        log.info("Processing ACCOUNT_LOCKED event - UserId: {}, Email: {}, Reason: {}, FailedAttempts: {}, LastFailedIP: {}",
                event.getUserId(), event.getEmail(), event.getReason(),
                event.getFailedAttempts(), event.getLastFailedIp());
        // TODO: Store security metrics (lockout frequency, failed attempts, IP tracking, etc.)
    }

    private void processAuthenticationFailed(AuthenticationFailedEvent event) {
        log.info("Processing AUTHENTICATION_FAILED event - UserId: {}, Identifier: {}, FailureReason: {}, IP: {}, FailedAttempts: {}, Device: {}",
                event.getUserId(), event.getIdentifier(), event.getFailureReason(),
                event.getIpAddress(), event.getFailedAttempts(), event.getDeviceInfo());
        // TODO: Store authentication failure metrics (failure count, IP tracking, brute-force detection, failure reason distribution, etc.)
    }
}
