package com.example.gateway.service.proxy;

import com.example.gateway.client.CartServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CartProxyAdapter implements ProxyAdapter {

    private final CartServiceClient cartServiceClient;

    @Override
    public ResponseEntity<Object> get(String path, Map<String, String> queryParams) {
        return cartServiceClient.forwardGetRequest(path, queryParams);
    }

    @Override
    public ResponseEntity<Object> post(String path, Map<String, String> queryParams, Object body) {
        return cartServiceClient.forwardPostRequest(path, queryParams, body);
    }

    @Override
    public ResponseEntity<Object> put(String path, Map<String, String> queryParams, Object body) {
        return cartServiceClient.forwardPutRequest(path, queryParams, body);
    }

    @Override
    public ResponseEntity<Object> delete(String path, Map<String, String> queryParams) {
        return cartServiceClient.forwardDeleteRequest(path, queryParams);
    }
}
