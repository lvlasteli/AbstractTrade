package com.example.cart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LuaScriptService {

    private final RedisTemplate<String, Object> redisTemplate;
    private DefaultRedisScript<Map> addItemScript;
    private DefaultRedisScript<Map> updateItemScript;

    public LuaScriptService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        loadScripts();
    }

    private void loadScripts() {
        try {
            ClassPathResource addItemResource = new ClassPathResource("lua/add-item.lua");
            String addItemScriptContent = StreamUtils.copyToString(
                    addItemResource.getInputStream(), StandardCharsets.UTF_8);
            addItemScript = new DefaultRedisScript<>();
            addItemScript.setScriptText(addItemScriptContent);
            addItemScript.setResultType(Map.class);

            ClassPathResource updateItemResource = new ClassPathResource("lua/update-item.lua");
            String updateItemScriptContent = StreamUtils.copyToString(
                    updateItemResource.getInputStream(), StandardCharsets.UTF_8);
            updateItemScript = new DefaultRedisScript<>();
            updateItemScript.setScriptText(updateItemScriptContent);
            updateItemScript.setResultType(Map.class);

            log.info("Lua scripts loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load Lua scripts", e);
            throw new RuntimeException("Failed to load Lua scripts", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Integer executeAddItem(List<String> keys, List<String> args) {
        try {
            Map<String, Object> result = redisTemplate.execute(
                    addItemScript, keys, args.toArray());
            
            if (result == null) {
                throw new RuntimeException("Lua script returned null");
            }
            
            if (result.containsKey("err")) {
                String error = (String) result.get("err");
                throw new RuntimeException("Lua script error: " + error);
            }
            
            Object okValue = result.get("ok");
            if (okValue instanceof Number) {
                return ((Number) okValue).intValue();
            }
            
            throw new RuntimeException("Unexpected result format from Lua script");
        } catch (Exception e) {
            log.error("Error executing add-item Lua script", e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public Integer executeUpdateItem(List<String> keys, List<String> args) {
        try {
            Map<String, Object> result = redisTemplate.execute(
                    updateItemScript, keys, args.toArray());
            
            if (result == null) {
                throw new RuntimeException("Lua script returned null");
            }
            
            if (result.containsKey("err")) {
                String error = (String) result.get("err");
                if ("VERSION_MISMATCH".equals(error)) {
                    Object currentVersion = result.get("current");
                    throw new RuntimeException("VERSION_MISMATCH:" + currentVersion);
                }
                throw new RuntimeException("Lua script error: " + error);
            }
            
            Object okValue = result.get("ok");
            if (okValue instanceof Number) {
                return ((Number) okValue).intValue();
            }
            
            throw new RuntimeException("Unexpected result format from Lua script");
        } catch (Exception e) {
            log.error("Error executing update-item Lua script", e);
            throw e;
        }
    }
}
