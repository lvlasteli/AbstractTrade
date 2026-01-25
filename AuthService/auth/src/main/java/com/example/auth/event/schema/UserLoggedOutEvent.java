package com.example.auth.event.schema;

import com.example.auth.constants.KafkaConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UserLoggedOutEvent extends AuthEvent {

    public static final String EVENT_TYPE = KafkaConstants.USER_LOGGED_OUT;

    private UUID userId;
    private LogoutType logoutType;

    public enum LogoutType {
        USER_INITIATED,
        SESSION_EXPIRED,
        ADMIN_REVOKED
    }

    public static UserLoggedOutEvent create(UUID userId, String sessionId, LogoutType logoutType) {
        UserLoggedOutEvent event = UserLoggedOutEvent.builder()
                .userId(userId)
                .logoutType(logoutType)
                .build();
        event.initializeEvent(EVENT_TYPE, sessionId);
        return event;
    }
}
