package com.example.auth.service;

import com.example.auth.model.dto.request.RequestMetadata;
import com.example.auth.model.entity.AuthenticationEvent;
import com.example.auth.repository.AuthAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for logging authentication events to the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuthAuditRepository eventRepository;

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";
    public static final String REGISTER = "REGISTER";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String PASSWORD_RESET_REQUEST = "PASSWORD_RESET_REQUEST";
    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";

    public void logEvent(UUID userId, String eventType, String ipAddress,
                         String userAgent, String deviceInfo, String sessionToken,
                         String failureReason) {
        try {
            AuthenticationEvent event = AuthenticationEvent.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .deviceInfo(deviceInfo)
                    .sessionToken(sessionToken)
                    .failureReason(failureReason)
                    .build();

            eventRepository.save(event);
            log.debug("Logged authentication event: {} for user: {}", eventType, userId);
        } catch (Exception e) {
            log.error("Failed to log authentication event: {} - {}", eventType, e.getMessage(), e);
        }
    }

    public void logLoginSuccess(UUID userId, RequestMetadata metadata, String sessionToken) {
        logEvent(userId, LOGIN_SUCCESS, metadata.ipAddress(), metadata.userAgent(),
                metadata.deviceInfo(), sessionToken, null);
    }

    public void logLoginFailed(UUID userId, RequestMetadata metadata, String failureReason) {
        logEvent(userId, LOGIN_FAILED, metadata.ipAddress(), metadata.userAgent(),
                metadata.deviceInfo(), null, failureReason);
    }


    public void logLogout(UUID userId, RequestMetadata metadata, String sessionToken) {
        logEvent(userId, LOGOUT, metadata.ipAddress(), metadata.userAgent(),
                metadata.deviceInfo(), sessionToken, null);
    }

    public void logRegister(UUID userId, RequestMetadata metadata) {
        logEvent(userId, REGISTER, metadata.ipAddress(), metadata.userAgent(),
                metadata.deviceInfo(), null, null);
    }

    public void logAccountLocked(UUID userId, RequestMetadata metadata) {
        var msg ="Maximum failed login attempts exceeded";
        logEvent(userId, ACCOUNT_LOCKED, metadata.ipAddress(), metadata.userAgent(),
                metadata.deviceInfo(), null, msg);
    }

    public void logPasswordResetRequest(UUID userId, RequestMetadata metadata) {
        logEvent(userId, PASSWORD_RESET_REQUEST, metadata.ipAddress(), metadata.userAgent(),
                metadata.deviceInfo(), null, null);
    }

    public void logPasswordChanged(UUID userId, RequestMetadata metadata, String sessionToken) {
        logEvent(userId, PASSWORD_CHANGED, metadata.ipAddress(), metadata.userAgent(),
                metadata.deviceInfo(), sessionToken, null);
    }
}
