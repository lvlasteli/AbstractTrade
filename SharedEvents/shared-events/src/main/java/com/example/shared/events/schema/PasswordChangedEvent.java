package com.example.shared.events.schema;

import com.example.shared.events.constants.KafkaConstants;
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
public class PasswordChangedEvent extends AuthEvent {

    public static final String EVENT_TYPE = KafkaConstants.PASSWORD_CHANGED;

    private UUID userId;
    private String email;
    private ChangeType changeType;
    private String ipAddress;

    public enum ChangeType {
        USER_RESET,
        ADMIN_RESET,
        FORGOT_PASSWORD
    }

    public static PasswordChangedEvent create(UUID userId, String email, String sessionId,
                                              ChangeType changeType, String ipAddress) {
        PasswordChangedEvent event = PasswordChangedEvent.builder()
                .userId(userId)
                .email(email)
                .changeType(changeType)
                .ipAddress(ipAddress)
                .build();
        event.initializeEvent(EVENT_TYPE, sessionId);
        return event;
    }
}
