package com.example.auth.service;

import com.example.auth.constants.ErrorMsg;
import com.example.auth.event.AuthEventPublisher;
import com.example.shared.events.schema.AccountLockedEvent;
import com.example.shared.events.schema.AuthenticationFailedEvent;
import com.example.shared.events.schema.UserLoggedInEvent;
import com.example.shared.events.schema.UserLoggedOutEvent;
import com.example.auth.exception.AccountLockedException;
import com.example.auth.exception.InvalidCredentialsException;
import com.example.auth.exception.SessionNotFoundException;
import com.example.auth.model.dto.request.LoginRequest;
import com.example.auth.model.dto.request.RequestMetadata;
import com.example.auth.model.dto.response.SessionValidationResponse;
import com.example.auth.model.dto.response.UserInfoResponse;
import com.example.auth.model.entity.User;
import com.example.auth.model.session.UserSession;
import com.example.auth.security.AccountLockoutManager;
import com.example.auth.security.RateLimitManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;
    private final SessionService sessionService;
    private final RateLimitManager rateLimitManager;
    private final AuditService auditService;
    private final AuthEventPublisher eventPublisher;
    private final AccountLockoutManager lockoutManager;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionFactory sessionFactory;

    @Transactional
    public String login(LoginRequest request, RequestMetadata metadata) {
        User user = authenticate(request, metadata);

        rateLimitManager.clearFailedLoginAttempts(user.getId());
        userService.resetUserAttempts(user);

        UserSession session = sessionFactory.create(user, metadata);
        String sessionId = sessionService.createSession(session);

        auditService.logLoginSuccess(user.getId(), metadata, sessionId);

        eventPublisher.publish(UserLoggedInEvent.create(
                user.getId(),
                user.getEmail(),
                sessionId,
                metadata.ipAddress(),
                metadata.userAgent(),
                metadata.deviceInfo()
        ));

        return sessionId;
    }

    public void logout(String sessionId, RequestMetadata metadata) {
        UserSession session = sessionService.getSession(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(ErrorMsg.SESSION_NOT_FOUND));

        sessionService.deleteSession(sessionId);

        auditService.logLogout(session.getUserId(), metadata, sessionId);

        eventPublisher.publish(UserLoggedOutEvent.create(
                session.getUserId(),
                sessionId,
                UserLoggedOutEvent.LogoutType.USER_INITIATED
        ));

        log.info("User logged out: {}", session.getEmail());
    }

    public SessionValidationResponse validateSession(String sessionId) {
        return sessionService.getSession(sessionId)
                .map(session -> SessionValidationResponse.builder()
                        .valid(true)
                        .userId(session.getUserId())
                        .email(session.getEmail())
                        .roles(session.getRoles())
                        .permissions(session.getPermissions())
                        .build())
                .orElse(SessionValidationResponse.builder()
                        .valid(false)
                        .build());
    }

    public boolean refreshSession(String sessionId) {
        return sessionService.extendSession(sessionId);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(String sessionId) {
        UserSession session = sessionService.getSession(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(ErrorMsg.SESSION_NOT_FOUND));

        User user = userService.findByIdWithRoles(session.getUserId())
                .orElseThrow(() -> new SessionNotFoundException(ErrorMsg.USER_NOT_FOUND));

        return UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(session.getRoles())
                .permissions(session.getPermissions())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    private User authenticate(LoginRequest request, RequestMetadata metadata) {
        User user = userService.findByEmailOrUsernameWithRoles(request.getIdentifier())
                .orElseThrow(() -> {
                    auditService.logLoginFailed(null, metadata, ErrorMsg.USER_NOT_FOUND);
                    eventPublisher.publish(AuthenticationFailedEvent.create(
                            null,
                            request.getIdentifier(),
                            ErrorMsg.USER_NOT_FOUND,
                            metadata.ipAddress(),
                            metadata.userAgent(),
                            metadata.deviceInfo(),
                            0L
                    ));
                    return new InvalidCredentialsException(ErrorMsg.INVALID_CREDENTIALS);
                });

        // Check database-level lock first
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            if (!lockoutManager.isAccountLocked(user.getId())) {
                userService.unlockAccount(user);
                user = userService.findByIdWithRoles(user.getId())
                        .orElseThrow(() -> new InvalidCredentialsException(ErrorMsg.USER_NOT_FOUND));
            }
        }

        if (lockoutManager.isAccountLocked(user.getId())) {
            auditService.logLoginFailed(user.getId(), metadata, ErrorMsg.ACCOUNT_LOCKED);
            throw new AccountLockedException(ErrorMsg.ACCOUNT_IS_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, metadata);
            throw new InvalidCredentialsException(ErrorMsg.INVALID_CREDENTIALS);
        }

        if (rateLimitManager.isUserRateLimited(user.getId())) {
            handleAccountLockout(user, metadata);
            throw new AccountLockedException(ErrorMsg.ACCOUNT_LOCKED_TOO_MANY_ATTEMPTS);
        }

        return user;
    }

    private void handleFailedLogin(User user, RequestMetadata metadata) {
        long attempts = rateLimitManager.recordFailedLoginByUser(user.getId());

        auditService.logLoginFailed(user.getId(), metadata, ErrorMsg.INVALID_CREDENTIALS);

        eventPublisher.publish(AuthenticationFailedEvent.create(
                user.getId(),
                user.getEmail(),
                ErrorMsg.INVALID_CREDENTIALS,
                metadata.ipAddress(),
                metadata.userAgent(),
                metadata.deviceInfo(),
                attempts
        ));

        if (lockoutManager.shouldLockAccount(attempts)) {
            handleAccountLockout(user, metadata);
        }
    }

    private void handleAccountLockout(User user, RequestMetadata metadata) {
        int lockoutMinutes = lockoutManager.lockAccount(user.getId());
        long failedAttempts = rateLimitManager.getFailedLoginCountByUser(user.getId());

        userService.lockAccount(user);

        auditService.logAccountLocked(user.getId(), metadata);

        eventPublisher.publish(AccountLockedEvent.create(
                user.getId(),
                user.getEmail(),
                AccountLockedEvent.LockReason.MAX_FAILED_ATTEMPTS,
                (int) failedAttempts,
                lockoutMinutes,
                metadata.ipAddress()
        ));
    }
}
