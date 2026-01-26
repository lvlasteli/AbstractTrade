package com.example.auth.service;

import com.example.auth.constants.ErrorMsg;
import com.example.auth.event.AuthEventPublisher;
import com.example.shared.events.schema.PasswordChangedEvent;
import com.example.shared.events.schema.PasswordResetRequestedEvent;
import com.example.auth.exception.InvalidCredentialsException;
import com.example.auth.exception.RateLimitExceededException;
import com.example.auth.model.dto.request.PasswordForgotRequest;
import com.example.auth.model.dto.request.PasswordResetRequest;
import com.example.auth.model.dto.request.RequestMetadata;
import com.example.auth.model.entity.User;
import com.example.auth.security.AccountLockoutManager;
import com.example.auth.security.RateLimitManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryService {

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final RateLimitManager rateLimitManager;
    private final AuditService auditService;
    private final AuthEventPublisher eventPublisher;
    private final AccountLockoutManager lockoutManager;

    @Transactional
    public void initiatePasswordReset(PasswordForgotRequest request, RequestMetadata metadata) {
        User user = userService.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            log.debug("Ignoring password reset for unknown email: {}", request.getEmail());
            return;
        }

        if (rateLimitManager.isPasswordResetRateLimited(user.getId())) {
            throw new RateLimitExceededException(ErrorMsg.TOO_MANY_PASSWORD_RESET_REQUESTS);
        }

        rateLimitManager.recordPasswordResetRequest(user.getId());

        String token = passwordResetService.createResetToken(user.getId());
        auditService.logPasswordResetRequest(user.getId(), metadata);

        eventPublisher.publish(PasswordResetRequestedEvent.create(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                token,
                metadata.ipAddress()
        ));
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request, RequestMetadata metadata) {
        UUID userId = passwordResetService.validateToken(request.getToken())
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMsg.INVALID_OR_EXPIRED_RESET_TOKEN));

        User user = userService.findByIdWithRoles(userId)
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMsg.USER_NOT_FOUND));

        userService.updatePassword(user, request.getNewPassword());
        passwordResetService.invalidateToken(request.getToken());

        // Unlock account in both Redis and database
        lockoutManager.unlockAccount(user.getId());
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            userService.unlockAccount(user);
        }
        rateLimitManager.clearFailedLoginAttempts(user.getId());

        auditService.logPasswordChanged(user.getId(), metadata, null);

        eventPublisher.publish(PasswordChangedEvent.create(
                user.getId(),
                user.getEmail(),
                null,
                PasswordChangedEvent.ChangeType.FORGOT_PASSWORD,
                metadata.ipAddress()
        ));
    }
}
