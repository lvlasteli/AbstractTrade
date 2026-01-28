package com.example.product.service;

import com.example.product.constants.RedisConstants;
import com.example.product.model.dto.response.CategoryResponse;
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
public class CategoryCacheService {
    
    private final RedisTemplate<String, CategoryResponse> redisTemplate;
    
    @Value("${product.category.ttl-seconds:3600}")
    private long cacheTtlSeconds;
    
    
    public Optional<CategoryResponse> getCachedCategory(UUID categoryId) {
        if (categoryId == null) {
            return Optional.empty();
        }

        try {
            String key = RedisConstants.CATEGORY_PREFIX + categoryId.toString();
            CategoryResponse category = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            log.warn("Failed to retrieve category from cache for id {}: {}", categoryId, e.getMessage());
            return Optional.empty();
        }
    }
    
    public void cacheCategory(UUID categoryId, CategoryResponse category) {

        String key = RedisConstants.CATEGORY_PREFIX + categoryId.toString();
        redisTemplate.opsForValue().set(key, category, Duration.ofSeconds(cacheTtlSeconds));
        log.debug("Cached category {} with TTL {} seconds", categoryId, cacheTtlSeconds);

    }
    
    public void evictCategory(UUID categoryId) {
        String key = RedisConstants.CATEGORY_PREFIX  + categoryId.toString();
        redisTemplate.delete(key);
        log.debug("Evicted category {} from cache", categoryId);
    }
}
