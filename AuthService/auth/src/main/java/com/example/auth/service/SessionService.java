package com.example.auth.service;

import com.example.auth.constants.RedisConstants;
import com.example.auth.model.session.UserSession;
import com.example.auth.util.SecureTokenGenerator;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final RedisTemplate<String, UserSession> redisTemplate;

    @Value("${auth.session.ttl-hours:24}")
    private int sessionTtlHours;


    public String createSession(UserSession session) {
        String sessionId = SecureTokenGenerator.generate(32);
        String key = RedisConstants.SESSION_PREFIX + sessionId;

        Duration ttl = Duration.ofHours(sessionTtlHours);
        session.setSessionId(sessionId);
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(ttl));

        redisTemplate.opsForValue().set(key, session, ttl);
        log.debug("Created session for user: {}", session.getUserId());

        return sessionId;
    }

    public Optional<UserSession> getSession(String sessionId) {
         if (StringUtils.isEmpty(sessionId)) {
            return Optional.empty();
        }

        String key = RedisConstants.SESSION_PREFIX + sessionId;
        UserSession session = redisTemplate.opsForValue().get(key);

        if (session != null && session.getExpiresAt() != null && session.getExpiresAt().isBefore(Instant.now())) {
            deleteSession(sessionId);
            return Optional.empty();
        }

        return Optional.ofNullable(session);
    }

    public void deleteSession(String sessionId) {
         if (StringUtils.isEmpty(sessionId)) {
            return;
        }

        String key = RedisConstants.SESSION_PREFIX + sessionId;
        Boolean deleted = redisTemplate.delete(key);
        log.debug("Deleted session: {}, success: {}", sessionId, deleted);
    }

    public boolean extendSession(String sessionId) {
         if (StringUtils.isEmpty(sessionId)) {
            return false;
        }

        String key = RedisConstants.SESSION_PREFIX + sessionId;
        UserSession session = redisTemplate.opsForValue().get(key);

        if (session == null) {
            return false;
        }

        Duration ttl = Duration.ofHours(sessionTtlHours);
        session.setExpiresAt(Instant.now().plus(ttl));
        redisTemplate.opsForValue().set(key, session, ttl);
        log.debug("Extended session: {}", sessionId);

        return true;
    }

    public boolean isSessionValid(String sessionId) {
        return getSession(sessionId).isPresent();
    }
}
