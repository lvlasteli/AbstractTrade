package com.example.gateway.service;

import com.example.gateway.constants.RedisConstants;
import com.example.gateway.event.GatewayEventPublisher;
import com.example.gateway.event.schema.IpBlockedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
@Slf4j
public class IpBlockingService {

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final GatewayEventPublisher eventPublisher;

    @Value("${gateway.ip-block.duration-minutes:60}")
    private int ipBlockDurationMinutes;

    public boolean isIpBlocked(String ipAddress) {
        String key = RedisConstants.LOCKOUT_IP_PREFIX + ipAddress;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }


    public void blockIp(String ipAddress) {
        blockIp(ipAddress, "Rate limit exceeded", null, null);
    }

    public void blockIp(String ipAddress, String reason, Long failedAttempts, String userAgent) {
        String key = RedisConstants.LOCKOUT_IP_PREFIX + ipAddress;
        stringRedisTemplate.opsForValue().set(key, "blocked", Duration.ofMinutes(ipBlockDurationMinutes));
        log.warn("IP blocked: {} for {} minutes, reason: {}", ipAddress, ipBlockDurationMinutes, reason);

        IpBlockedEvent event = IpBlockedEvent.create(
                ipAddress,
                reason,
                ipBlockDurationMinutes,
                failedAttempts,
                userAgent
        );
        eventPublisher.publish(event);
    }


    public void unblockIp(String ipAddress) {
        String key = RedisConstants.LOCKOUT_IP_PREFIX + ipAddress;
        stringRedisTemplate.delete(key);
        log.info("IP unblocked: {}", ipAddress);
    }
}
