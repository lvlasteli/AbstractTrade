package com.example.gateway.service.proxy;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ProxyAdapter {
    ResponseEntity<Object> get(String path, Map<String, String> queryParams);
    ResponseEntity<Object> post(String path, Map<String, String> queryParams, Object body);
    ResponseEntity<Object> put(String path, Map<String, String> queryParams, Object body);
    ResponseEntity<Object> delete(String path, Map<String, String> queryParams);
}
