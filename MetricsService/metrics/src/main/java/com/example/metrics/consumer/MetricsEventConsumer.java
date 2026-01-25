package com.example.metrics.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer for metric events from auth.metrics Kafka topic.
 * Currently logs received messages. Future implementation will store metrics in time-series database.
 */
@Component
@Slf4j
public class MetricsEventConsumer {

    @KafkaListener(
            topics = "${metrics.kafka.topic:auth.metrics}",
            groupId = "${spring.kafka.consumer.group-id:metrics-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMetricEvent(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(value = "eventType", required = false) String eventType
    ) {
        log.info("Received metric event from topic: {}, key: {}, eventType: {}", topic, key, eventType);
        log.info("Event payload: {}", event);
        
        // TODO: Implement time-series database integration (InfluxDB/TimescaleDB)
        // TODO: Parse event and extract metric data
        // TODO: Store metrics in time-series database
        // TODO: Implement metric aggregation logic
        // TODO: Add error handling and retry logic
    }
}
