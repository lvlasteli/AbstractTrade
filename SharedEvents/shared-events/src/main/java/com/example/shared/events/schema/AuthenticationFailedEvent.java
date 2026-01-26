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
public class AuthenticationFailedEvent extends AuthEvent {

    public static final String EVENT_TYPE = KafkaConstants.AUTHENTICATION_FAILED;

    private UUID userId;
    private String identifier;
    private String failureReason;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private Long failedAttempts;

    public static AuthenticationFailedEvent create(UUID userId, String identifier, String failureReason,
                                                   String ipAddress, String userAgent, String deviceInfo,
                                                   Long failedAttempts) {
        AuthenticationFailedEvent event = AuthenticationFailedEvent.builder()
                .userId(userId)
                .identifier(identifier)
                .failureReason(failureReason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceInfo(deviceInfo)
                .failedAttempts(failedAttempts)
                .build();
        event.initializeEvent(EVENT_TYPE, null); // No session when authentication fails
        return event;
    }
}
