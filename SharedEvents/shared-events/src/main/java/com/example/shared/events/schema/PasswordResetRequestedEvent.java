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
public class PasswordResetRequestedEvent extends AuthEvent {

    public static final String EVENT_TYPE = KafkaConstants.PASSWORD_RESET_REQUESTED;

    private UUID userId;
    private String email;
    private String username;
    private String resetToken;
    private String ipAddress;

    public static PasswordResetRequestedEvent create(UUID userId, String email, String username,
                                                     String resetToken, String ipAddress) {
        PasswordResetRequestedEvent event = PasswordResetRequestedEvent.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .resetToken(resetToken)
                .ipAddress(ipAddress)
                .build();
        event.initializeEvent(EVENT_TYPE, null);
        return event;
    }
}
