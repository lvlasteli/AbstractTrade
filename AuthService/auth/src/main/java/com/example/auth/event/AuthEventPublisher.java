package com.example.auth.event;

import com.example.auth.event.schema.AuthEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;

    @Value("${auth.kafka.topic:auth.events}")
    private String topic;

    public void publish(AuthEvent event) {
        try {
            Message<AuthEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, event.getEventId())
                    .setHeader("eventType", event.getEventType())
                    .build();

            kafkaTemplate.send(message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event: {} - {}", event.getEventType(), ex.getMessage());
                        } else {
                            log.debug("Published event: {} with id: {}", event.getEventType(), event.getEventId());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing event: {} - {}", event.getEventType(), e.getMessage(), e);
        }
    }
}
