package com.example.gateway.event;

import com.example.gateway.event.schema.GatewayEvent;
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
public class GatewayEventPublisher {

    private final KafkaTemplate<String, GatewayEvent> kafkaTemplate;

    @Value("${gateway.kafka.topic:auth_metrics}")
    private String topic;

    public void publish(GatewayEvent event) {
        try {
            Message<GatewayEvent> message = MessageBuilder
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
