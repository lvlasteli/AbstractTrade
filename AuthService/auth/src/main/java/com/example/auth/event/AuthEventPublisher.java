package com.example.auth.event;

import com.example.shared.events.constants.KafkaConstants;
import com.example.shared.events.schema.AuthEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;

    @Value("${auth.kafka.topic.notifications:auth_notifications}")
    private String notificationsTopic;

    @Value("${auth.kafka.topic.metrics:auth_metrics}")
    private String metricsTopic;

    private static final Set<String> NOTIFICATION_EVENTS = Set.of(
            KafkaConstants.PASSWORD_RESET_REQUESTED,
            KafkaConstants.ACCOUNT_LOCKED,
            KafkaConstants.USER_REGISTERED,
            KafkaConstants.PASSWORD_CHANGED
    );

    private static final Set<String> METRICS_EVENTS = Set.of(
            KafkaConstants.USER_LOGGED_IN,
            KafkaConstants.USER_LOGGED_OUT,
            KafkaConstants.USER_REGISTERED,
            KafkaConstants.ACCOUNT_LOCKED
    );

    public void publish(AuthEvent event) {
        String eventType = event.getEventType();
        
        if (NOTIFICATION_EVENTS.contains(eventType)) {
            publishToTopic(event, notificationsTopic);
        }
        
        if (METRICS_EVENTS.contains(eventType)) {
            publishToTopic(event, metricsTopic);
        }
    }

    private void publishToTopic(AuthEvent event, String topic) {
        try {
            log.info("Type event: {} with id: {} to topic: {}",
                    event.getEventType(), event.getEventId(), topic);

            Message<AuthEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, event.getEventId())
                    .setHeader("eventType", event.getEventType())
                    .build();

            kafkaTemplate.send(message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event: {} to topic: {} - {}", 
                                    event.getEventType(), topic, ex.getMessage());
                        } else {
                            log.debug("Published event: {} with id: {} to topic: {}", 
                                    event.getEventType(), event.getEventId(), topic);
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing event: {} to topic: {} - {}", 
                    event.getEventType(), topic, e.getMessage(), e);
        }
    }
}
