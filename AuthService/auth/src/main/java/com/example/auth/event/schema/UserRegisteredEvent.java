package com.example.auth.event.schema;

import com.example.auth.constants.KafkaConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends AuthEvent {

    public static final String EVENT_TYPE = KafkaConstants.USER_REGISTERED;

    private UUID userId;
    private String email;
    private String username;
    private Set<String> roles;

    public static UserRegisteredEvent create(UUID userId, String email, String username, Set<String> roles, String sessionId) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .roles(roles)
                .build();
        event.initializeEvent(EVENT_TYPE, sessionId);
        return event;
    }
}
