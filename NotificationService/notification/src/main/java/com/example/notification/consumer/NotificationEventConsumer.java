package com.example.notification.consumer;

import com.example.shared.events.schema.AuthEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventConsumer {

    @KafkaListener(
            topics = "${notification.kafka.topic:auth_notifications}",
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotificationEvent(
            @Payload AuthEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(value = "eventType", required = false) String eventType,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        try {
            log.info("=".repeat(80));
            log.info("Received notification event:");
            log.info("  Topic: {}", topic);
            log.info("  Partition: {}", partition);
            log.info("  Offset: {}", offset);
            log.info("  Key: {}", key);
            log.info("  Event Type: {}", event != null ? event.getEventType() : eventType);
            log.info("  Event Payload: {}", event);
            log.info("=".repeat(80));
            
            // Extract event details
            if (event != null) {
                log.info("Event ID: {}, Timestamp: {}", event.getEventId(), event.getTimestamp());
            }
            
            // TODO: Implement notification processing logic
            // TODO: Parse event and extract notification data (userId, email, template type, etc.)
            // TODO: Check user notification preferences
            // TODO: Select appropriate notification channel (email/SMS/push)
            // TODO: Load notification template
            // TODO: Send notification via appropriate provider
            // TODO: Store notification record in database
            // TODO: Handle delivery failures and retries
            // TODO: Add error handling and dead-letter queue support
            
        } catch (Exception e) {
            log.error("Error processing notification event from topic: {}, key: {}", topic, key, e);
            throw e; // Re-throw to trigger error handler
        }
    }
}
