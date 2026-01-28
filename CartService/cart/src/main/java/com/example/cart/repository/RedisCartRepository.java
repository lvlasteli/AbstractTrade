package com.example.cart.repository;

import com.example.cart.constants.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisCartRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public String getUserCartKey(String userId) {
        return RedisConstants.USER_CARTS_PREFIX + userId;
    }

    public String getUserCartItemsKey(String userId) {
        return RedisConstants.USER_CART_ITEMS_PREFIX + userId;
    }

    public String getAnonCartKey(String cartId) {
        return RedisConstants.ANON_CARTS_PREFIX + cartId;
    }

    public String getAnonCartItemsKey(String cartId) {
        return RedisConstants.ANON_CART_ITEMS_PREFIX + cartId;
    }

    public Map<Object, Object> getCartMetadata(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Map<Object, Object> getCartItems(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void setCartMetadata(String key, Map<String, String> metadata) {
        redisTemplate.opsForHash().putAll(key, metadata);
    }

    public void setCartItem(String itemsKey, String sku, Integer quantity) {
        redisTemplate.opsForHash().put(itemsKey, "sku:" + sku, quantity.toString());
    }

    public void removeCartItem(String itemsKey, String sku) {
        redisTemplate.opsForHash().delete(itemsKey, "sku:" + sku);
    }

    public void deleteCart(String cartKey, String itemsKey) {
        redisTemplate.delete(cartKey);
        redisTemplate.delete(itemsKey);
    }

    public void refreshTtl(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    public Long incrementVersion(String cartKey) {
        return redisTemplate.opsForHash().increment(cartKey, "version", 1);
    }

    public void setField(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public String getField(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return value != null ? value.toString() : null;
    }

    public boolean exists(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
