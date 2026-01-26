package com.example.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitManager {

    private static final String RATE_LOGIN_USER_PREFIX = "rate:login:user:";
    private static final String RATE_PASSWORD_RESET_PREFIX = "rate:password-reset:user:";

    private final RedisTemplate<String, String> stringRedisTemplate;

    @Value("${auth.rate-limit.login.user.max-attempts:5}")
    private int loginUserMaxAttempts;

    @Value("${auth.rate-limit.login.user.window-minutes:15}")
    private int loginUserWindowMinutes;

    @Value("${auth.rate-limit.password-reset.max-attempts:3}")
    private int passwordResetMaxAttempts;

    @Value("${auth.rate-limit.password-reset.window-minutes:60}")
    private int passwordResetWindowMinutes;

    public long recordFailedLoginByUser(UUID userId) {
        String key = RATE_LOGIN_USER_PREFIX + userId.toString();
        return incrementWithExpiry(key, Duration.ofMinutes(loginUserWindowMinutes));
    }

    public boolean isUserRateLimited(UUID userId) {
        String key = RATE_LOGIN_USER_PREFIX + userId.toString();
        long attempts = getAttemptCount(key);
        return attempts >= loginUserMaxAttempts;
    }

    public long getFailedLoginCountByUser(UUID userId) {
        return getAttemptCount(RATE_LOGIN_USER_PREFIX + userId.toString());
    }

    public void clearFailedLoginAttempts(UUID userId) {
        String key = RATE_LOGIN_USER_PREFIX + userId.toString();
        stringRedisTemplate.delete(key);
        log.debug("Cleared failed login attempts for user: {}", userId);
    }

    public void recordPasswordResetRequest(UUID userId) {
        String key = RATE_PASSWORD_RESET_PREFIX + userId.toString();
        incrementWithExpiry(key, Duration.ofMinutes(passwordResetWindowMinutes));
    }


    public boolean isPasswordResetRateLimited(UUID userId) {
        String key = RATE_PASSWORD_RESET_PREFIX + userId.toString();
        long attempts = getAttemptCount(key);
        return attempts >= passwordResetMaxAttempts;
    }


    private long incrementWithExpiry(String key, Duration expiry) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 1L;
        }
        
        if (count == 1) {
            stringRedisTemplate.expire(key, expiry);
        }
        
        log.debug("Incremented rate limit key: {} to count: {}", key, count);
        return count;
    }

    private long getAttemptCount(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid rate limit value for key {}: {}", key, value);
            return 0;
        }
    }
}
