package com.example.metrics.config;

import com.example.shared.events.constants.KafkaConstants;
import com.example.shared.events.schema.AccountLockedEvent;
import com.example.shared.events.schema.AuthEvent;
import com.example.shared.events.schema.UserLoggedInEvent;
import com.example.shared.events.schema.UserLoggedOutEvent;
import com.example.shared.events.schema.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AuthEventDeserializer implements Deserializer<AuthEvent> {

    private static final Pattern EVENT_TYPE_PATTERN = Pattern.compile("\"eventType\"\\s*:\\s*\"([^\"]+)\"");
    
    private final Map<String, JacksonJsonDeserializer<? extends AuthEvent>> deserializers;

    public AuthEventDeserializer() {
        this.deserializers = new HashMap<>();
        deserializers.put(KafkaConstants.USER_LOGGED_IN, createDeserializer(UserLoggedInEvent.class));
        deserializers.put(KafkaConstants.USER_LOGGED_OUT, createDeserializer(UserLoggedOutEvent.class));
        deserializers.put(KafkaConstants.USER_REGISTERED, createDeserializer(UserRegisteredEvent.class));
        deserializers.put(KafkaConstants.ACCOUNT_LOCKED, createDeserializer(AccountLockedEvent.class));
    }
    
    private <T extends AuthEvent> JacksonJsonDeserializer<T> createDeserializer(Class<T> eventClass) {
        JacksonJsonDeserializer<T> deserializer = new JacksonJsonDeserializer<>(eventClass);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);
        return deserializer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }

    @Override
    public AuthEvent deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            String jsonString = new String(data, StandardCharsets.UTF_8);
            String eventType = extractEventType(jsonString);
            
            if (eventType == null) {
                log.error("Cannot extract eventType from JSON. Attempting to deserialize with all known types.");
                return tryDeserializeWithAllTypes(topic, data);
            }

            JacksonJsonDeserializer<? extends AuthEvent> deserializer = deserializers.get(eventType);
            if (deserializer == null) {
                log.warn("Unknown event type: {}. Attempting to deserialize with all known types.", eventType);
                return tryDeserializeWithAllTypes(topic, data);
            }

            return deserializer.deserialize(topic, null, data);
        } catch (Exception e) {
            log.error("Error deserializing AuthEvent from topic: {}", topic, e);
            throw new RuntimeException("Failed to deserialize AuthEvent", e);
        }
    }

    private String extractEventType(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        Matcher matcher = EVENT_TYPE_PATTERN.matcher(jsonString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private AuthEvent tryDeserializeWithAllTypes(String topic, byte[] data) {
        for (Map.Entry<String, JacksonJsonDeserializer<? extends AuthEvent>> entry : deserializers.entrySet()) {
            try {
                AuthEvent event = entry.getValue().deserialize(topic, null, data);
                if (event != null) {
                    log.debug("Successfully deserialized as {} event", entry.getKey());
                    return event;
                }
            } catch (Exception e) {
                log.trace("Failed to deserialize as {}: {}", entry.getKey(), e.getMessage());
            }
        }
        throw new RuntimeException("Failed to deserialize AuthEvent: could not determine event type and all deserialization attempts failed");
    }

    @Override
    public void close() {
        deserializers.values().forEach(JacksonJsonDeserializer::close);
    }
}
