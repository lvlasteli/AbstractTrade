package com.example.gateway.service;

import com.example.gateway.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
@Slf4j
public class IpRateLimitService {

    private final RedisTemplate<String, String> stringRedisTemplate;

    @Value("${gateway.rate-limit.login.ip.max-attempts:10}")
    private int loginIpMaxAttempts;

    @Value("${gateway.rate-limit.login.ip.window-minutes:15}")
    private int loginIpWindowMinutes;

    @Value("${gateway.rate-limit.register.ip.max-attempts:5}")
    private int registerIpMaxAttempts;

    @Value("${gateway.rate-limit.register.ip.window-minutes:60}")
    private int registerIpWindowMinutes;


    public long recordFailedLoginByIp(String ipAddress) {
        String key = RedisConstants.RATE_LOGIN_IP_PREFIX + ipAddress;
        return incrementWithExpiry(key, Duration.ofMinutes(loginIpWindowMinutes));
    }


    public boolean isIpRateLimited(String ipAddress) {
        String key = RedisConstants.RATE_LOGIN_IP_PREFIX + ipAddress;
        long attempts = getAttemptCount(key);
        return attempts >= loginIpMaxAttempts;
    }


    public void recordRegistrationByIp(String ipAddress) {
        String key = RedisConstants.RATE_REGISTER_IP_PREFIX + ipAddress;
        incrementWithExpiry(key, Duration.ofMinutes(registerIpWindowMinutes));
    }


    public boolean isRegistrationRateLimited(String ipAddress) {
        String key = RedisConstants.RATE_REGISTER_IP_PREFIX + ipAddress;
        long attempts = getAttemptCount(key);
        return attempts >= registerIpMaxAttempts;
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
