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
public class AccountLockedEvent extends AuthEvent {

    public static final String EVENT_TYPE = KafkaConstants.ACCOUNT_LOCKED;

    private UUID userId;
    private String email;
    private LockReason reason;
    private Integer failedAttempts;
    private Integer lockDurationMinutes;
    private String lastFailedIp;

    public enum LockReason {
        MAX_FAILED_ATTEMPTS,
        ADMIN_ACTION,
        SUSPICIOUS_ACTIVITY
    }

    public static AccountLockedEvent create(UUID userId, String email, LockReason reason,
                                            Integer failedAttempts, Integer lockDurationMinutes,
                                            String lastFailedIp) {
        AccountLockedEvent event = AccountLockedEvent.builder()
                .userId(userId)
                .email(email)
                .reason(reason)
                .failedAttempts(failedAttempts)
                .lockDurationMinutes(lockDurationMinutes)
                .lastFailedIp(lastFailedIp)
                .build();
        event.initializeEvent(EVENT_TYPE, null); // No session when account is locked
        return event;
    }
}
