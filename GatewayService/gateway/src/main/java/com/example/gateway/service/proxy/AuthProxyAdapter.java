package com.example.gateway.service.proxy;

import com.example.gateway.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthProxyAdapter implements ProxyAdapter {

    private final AuthServiceClient authServiceClient;

    @Override
    public ResponseEntity<Object> get(String path, Map<String, String> queryParams) {
        return authServiceClient.forwardGetRequest(path);
    }

    @Override
    public ResponseEntity<Object> post(String path, Map<String, String> queryParams, Object body) {
        return authServiceClient.forwardPostRequest(path, body);
    }

    @Override
    public ResponseEntity<Object> put(String path, Map<String, String> queryParams, Object body) {
        return authServiceClient.forwardPutRequest(path, body);
    }

    @Override
    public ResponseEntity<Object> delete(String path, Map<String, String> queryParams) {
        return authServiceClient.forwardDeleteRequest(path);
    }
}
