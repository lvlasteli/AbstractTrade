package com.example.auth.security;

import com.example.auth.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountLockoutManager {

    private final RedisTemplate<String, String> stringRedisTemplate;

    @Value("${auth.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.lockout.extended-max-attempts:10}")
    private int extendedMaxAttempts;

    @Value("${auth.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Value("${auth.lockout.extended-duration-minutes:60}")
    private int extendedLockoutDurationMinutes;

    @Value("${auth.lockout.max-duration-hours:24}")
    private int maxLockoutDurationHours;

    public boolean shouldLockAccount(long failedAttempts) {
        return failedAttempts >= maxAttempts;
    }

    public boolean shouldExtendLockout(long failedAttempts) {
        return failedAttempts >= extendedMaxAttempts;
    }

    public boolean isAccountLocked(UUID userId) {
        String key = RedisConstants.LOCKOUT_USER_PREFIX + userId.toString();
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public int lockAccount(UUID userId) {
        int level = incrementLockoutLevel(userId);
        Duration lockoutDuration = calculateLockoutDuration(level);
        
        String key = RedisConstants.LOCKOUT_USER_PREFIX + userId.toString();
        stringRedisTemplate.opsForValue().set(key, String.valueOf(level), lockoutDuration);
        
        log.warn("Account locked for user: {} at level: {} for {} minutes", 
                userId, level, lockoutDuration.toMinutes());
        
        return (int) lockoutDuration.toMinutes();
    }

    public void unlockAccount(UUID userId) {
        String lockoutKey = RedisConstants.LOCKOUT_USER_PREFIX + userId.toString();
        String levelKey = RedisConstants.LOCKOUT_LEVEL_PREFIX + userId.toString();
        
        stringRedisTemplate.delete(lockoutKey);
        stringRedisTemplate.delete(levelKey);
        
        log.info("Account unlocked for user: {}", userId);
    }

    private int incrementLockoutLevel(UUID userId) {
        String key = RedisConstants.LOCKOUT_LEVEL_PREFIX + userId.toString();
        Long level = stringRedisTemplate.opsForValue().increment(key);
        if (level == null) {
            level = 1L;
        }
        
        stringRedisTemplate.expire(key, Duration.ofHours(24));
        
        return level.intValue();
    }

    private Duration calculateLockoutDuration(int level) {
        return switch (level) {
            case 1 -> Duration.ofMinutes(lockoutDurationMinutes);
            case 2 -> Duration.ofMinutes(extendedLockoutDurationMinutes);
            default -> Duration.ofHours(maxLockoutDurationHours);
        };
    }
}
