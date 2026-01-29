package com.example.cart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LuaScriptService {

    private final RedisTemplate<String, Object> redisTemplate;
    private DefaultRedisScript<Map<String, Object>> addItemScript;
    private DefaultRedisScript<Map<String, Object>> updateItemScript;

    public LuaScriptService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        loadScripts();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void loadScripts() {
        try {
            ClassPathResource addItemResource = new ClassPathResource("lua/add-item.lua");
            String addItemScriptContent = StreamUtils.copyToString(
                    addItemResource.getInputStream(), StandardCharsets.UTF_8);
            addItemScript = new DefaultRedisScript<>();
            addItemScript.setScriptText(addItemScriptContent);
            addItemScript.setResultType((Class) Map.class);

            ClassPathResource updateItemResource = new ClassPathResource("lua/update-item.lua");
            String updateItemScriptContent = StreamUtils.copyToString(
                    updateItemResource.getInputStream(), StandardCharsets.UTF_8);
            updateItemScript = new DefaultRedisScript<>();
            updateItemScript.setScriptText(updateItemScriptContent);
            updateItemScript.setResultType((Class) Map.class);

            log.info("Lua scripts loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load Lua scripts", e);
            throw new RuntimeException("Failed to load Lua scripts", e);
        }
    }

    public Integer executeAddItem(List<String> keys, List<String> args) {
        try {
            log.info("Executing Lua script with keys: {} and args: {}", keys, args);
            
            // Execute script with explicit string serialization
            Object result = redisTemplate.execute((RedisCallback<Object>) connection -> {
                StringRedisSerializer serializer = new StringRedisSerializer();
                
                // Serialize keys
                byte[][] keyBytes = keys.stream()
                        .map(serializer::serialize)
                        .toArray(byte[][]::new);
                
                // Serialize args
                byte[][] argBytes = args.stream()
                        .map(serializer::serialize)
                        .toArray(byte[][]::new);
                
                // Execute script
                return connection.eval(
                        addItemScript.getScriptAsString().getBytes(StandardCharsets.UTF_8),
                        org.springframework.data.redis.connection.ReturnType.MULTI,
                        keyBytes.length,
                        concat(keyBytes, argBytes)
                );
            });
            
            log.info("Lua script result: {} (type: {})", result, result != null ? result.getClass().getName() : "null");
            
            if (result == null) {
                throw new RuntimeException("Lua script returned null");
            }
            
            // Parse result as List (Lua tables come back as Lists)
            if (result instanceof List<?> resultList) {
                return parseScriptResult(resultList);
            }
            
            throw new RuntimeException("Unexpected result type: " + result.getClass());
        } catch (Exception e) {
            log.error("Error executing add-item Lua script", e);
            throw new RuntimeException("Failed to execute add-item script", e);
        }
    }
    
    private byte[][] concat(byte[][] a, byte[][] b) {
        byte[][] result = new byte[a.length + b.length][];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    private Integer parseScriptResult(List<?> resultList) {
        // Lua tables come back as alternating key-value pairs in a list
        // e.g., ["ok", 1] or ["err", "error_message"]
        if (resultList.isEmpty()) {
            throw new RuntimeException("Lua script returned empty result");
        }
        
        for (int i = 0; i < resultList.size(); i += 2) {
            if (i + 1 >= resultList.size()) break;
            
            Object key = resultList.get(i);
            Object value = resultList.get(i + 1);
            
            String keyStr = new String((byte[]) key, StandardCharsets.UTF_8);
            
            if ("ok".equals(keyStr)) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else if (value instanceof byte[]) {
                    return Integer.parseInt(new String((byte[]) value, StandardCharsets.UTF_8));
                }
            } else if ("err".equals(keyStr)) {
                String error = value instanceof byte[] ? 
                        new String((byte[]) value, StandardCharsets.UTF_8) : 
                        String.valueOf(value);
                throw new RuntimeException("Lua script error: " + error);
            }
        }
        
        throw new RuntimeException("Unexpected script result format");
    }

    public Integer executeUpdateItem(List<String> keys, List<String> args) {
        try {
            log.info("Executing update Lua script with keys: {} and args: {}", keys, args);
            
            // Execute script with explicit string serialization
            Object result = redisTemplate.execute((RedisCallback<Object>) connection -> {
                StringRedisSerializer serializer = new StringRedisSerializer();
                
                // Serialize keys
                byte[][] keyBytes = keys.stream()
                        .map(serializer::serialize)
                        .toArray(byte[][]::new);
                
                // Serialize args
                byte[][] argBytes = args.stream()
                        .map(serializer::serialize)
                        .toArray(byte[][]::new);
                
                // Execute script
                return connection.eval(
                        updateItemScript.getScriptAsString().getBytes(StandardCharsets.UTF_8),
                        org.springframework.data.redis.connection.ReturnType.MULTI,
                        keyBytes.length,
                        concat(keyBytes, argBytes)
                );
            });
            
            log.info("Update Lua script result: {} (type: {})", result, result != null ? result.getClass().getName() : "null");
            
            if (result == null) {
                throw new RuntimeException("Lua script returned null");
            }
            
            // Parse result as List (Lua tables come back as Lists)
            if (result instanceof List<?> resultList) {
                return parseUpdateScriptResult(resultList);
            }
            
            throw new RuntimeException("Unexpected result type: " + result.getClass());
        } catch (Exception e) {
            log.error("Error executing update-item Lua script", e);
            throw e;
        }
    }
    
    private Integer parseUpdateScriptResult(List<?> resultList) {
        // Lua tables come back as alternating key-value pairs in a list
        if (resultList.isEmpty()) {
            throw new RuntimeException("Lua script returned empty result");
        }
        
        for (int i = 0; i < resultList.size(); i += 2) {
            if (i + 1 >= resultList.size()) break;
            
            Object key = resultList.get(i);
            Object value = resultList.get(i + 1);
            
            String keyStr = new String((byte[]) key, StandardCharsets.UTF_8);
            
            if ("ok".equals(keyStr)) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else if (value instanceof byte[]) {
                    return Integer.parseInt(new String((byte[]) value, StandardCharsets.UTF_8));
                }
            } else if ("err".equals(keyStr)) {
                String error = value instanceof byte[] ? 
                        new String((byte[]) value, StandardCharsets.UTF_8) : 
                        String.valueOf(value);
                
                // Check for version mismatch with current version
                if ("VERSION_MISMATCH".equals(error)) {
                    // Look for "current" key in the rest of the list
                    for (int j = i + 2; j < resultList.size(); j += 2) {
                        if (j + 1 >= resultList.size()) break;
                        Object k = resultList.get(j);
                        Object v = resultList.get(j + 1);
                        String kStr = new String((byte[]) k, StandardCharsets.UTF_8);
                        if ("current".equals(kStr)) {
                            String currentVersion = v instanceof byte[] ?
                                    new String((byte[]) v, StandardCharsets.UTF_8) :
                                    String.valueOf(v);
                            throw new RuntimeException("VERSION_MISMATCH:" + currentVersion);
                        }
                    }
                }
                
                throw new RuntimeException("Lua script error: " + error);
            }
        }
        
        throw new RuntimeException("Unexpected script result format");
    }
}
