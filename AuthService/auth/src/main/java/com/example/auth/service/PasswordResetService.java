package com.example.auth.service;

import com.example.auth.constants.RedisConstants;
import com.example.auth.util.SecureTokenGenerator;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final RedisTemplate<String, String> stringRedisTemplate;

    @Value("${auth.password-reset.ttl-minutes:30}")
    private int tokenTtlMinutes;

    public String createResetToken(UUID userId) {
        String token = SecureTokenGenerator.generate(32);
        String key = RedisConstants.PASSWORD_RESET_TOKEN_PREFIX + token;

        stringRedisTemplate.opsForValue().set(key, userId.toString(), Duration.ofMinutes(tokenTtlMinutes));
        log.debug("Created password reset token for user: {}", userId);

        return token;
    }

    public Optional<UUID> validateToken(String token) {
        if (StringUtils.isBlank(token)) {
            return Optional.empty();
        }

        String key = RedisConstants.PASSWORD_RESET_TOKEN_PREFIX + token;
        String userId = stringRedisTemplate.opsForValue().get(key);

        if (userId == null) {
            log.debug("Password reset token not found or expired: {}", token);
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID in reset token: {}", userId);
            return Optional.empty();
        }
    }

    public void invalidateToken(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }

        String key = RedisConstants.PASSWORD_RESET_TOKEN_PREFIX + token;
        stringRedisTemplate.delete(key);
        log.debug("Invalidated password reset token");
    }
}
