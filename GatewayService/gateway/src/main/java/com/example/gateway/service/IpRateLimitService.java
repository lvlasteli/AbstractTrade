package com.example.gateway.service;

import com.example.gateway.config.RateLimitProperties;
import com.example.gateway.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
@Slf4j
public class IpRateLimitService {

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RateLimitProperties rateLimitProperties;


    public long recordFailedLoginByIp(String ipAddress) {
        String key = RedisConstants.RATE_LOGIN_IP_PREFIX + ipAddress;
        return incrementWithExpiry(key, Duration.ofMinutes(rateLimitProperties.getLogin().getIp().getWindowMinutes()));
    }


    public boolean isIpRateLimited(String ipAddress) {
        String key = RedisConstants.RATE_LOGIN_IP_PREFIX + ipAddress;
        long attempts = getAttemptCount(key);
        return attempts >= rateLimitProperties.getLogin().getIp().getMaxAttempts();
    }


    public void recordRegistrationByIp(String ipAddress) {
        String key = RedisConstants.RATE_REGISTER_IP_PREFIX + ipAddress;
        incrementWithExpiry(key, Duration.ofMinutes(rateLimitProperties.getRegister().getIp().getWindowMinutes()));
    }


    public boolean isRegistrationRateLimited(String ipAddress) {
        String key = RedisConstants.RATE_REGISTER_IP_PREFIX + ipAddress;
        long attempts = getAttemptCount(key);
        return attempts >= rateLimitProperties.getRegister().getIp().getMaxAttempts();
    }

    public void recordCartCreation(String ipAddress) {
        String key = RedisConstants.RATE_CART_CREATION_IP_PREFIX + ipAddress;
        incrementWithExpiry(key, Duration.ofHours(rateLimitProperties.getCartCreation().getIp().getWindowHours()));
    }

    public boolean isCartCreationRateLimited(String ipAddress) {
        String key = RedisConstants.RATE_CART_CREATION_IP_PREFIX + ipAddress;
        long attempts = getAttemptCount(key);
        return attempts >= rateLimitProperties.getCartCreation().getIp().getMaxAttempts();
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
