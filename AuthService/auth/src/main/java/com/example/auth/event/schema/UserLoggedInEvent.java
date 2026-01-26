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
public class UserLoggedInEvent extends AuthEvent {

    public static final String EVENT_TYPE = KafkaConstants.USER_LOGGED_IN;

    private UUID userId;
    private String email;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;

    public static UserLoggedInEvent create(UUID userId, String email, String sessionId,
                                           String ipAddress, String userAgent, String deviceInfo) {
        UserLoggedInEvent event = UserLoggedInEvent.builder()
                .userId(userId)
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceInfo(deviceInfo)
                .build();
        event.initializeEvent(EVENT_TYPE, sessionId);
        return event;
    }
}
