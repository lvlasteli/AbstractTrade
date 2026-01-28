package com.example.gateway.service.proxy;

import com.example.gateway.client.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductProxyAdapter implements ProxyAdapter {

    private final ProductServiceClient productServiceClient;

    @Override
    public ResponseEntity<Object> get(String path, Map<String, String> queryParams) {
        return productServiceClient.forwardGetRequest(path, queryParams);
    }

    @Override
    public ResponseEntity<Object> post(String path, Map<String, String> queryParams, Object body) {
        return productServiceClient.forwardPostRequest(path, queryParams, body);
    }

    @Override
    public ResponseEntity<Object> put(String path, Map<String, String> queryParams, Object body) {
        return productServiceClient.forwardPutRequest(path, queryParams, body);
    }

    @Override
    public ResponseEntity<Object> delete(String path, Map<String, String> queryParams) {
        return productServiceClient.forwardDeleteRequest(path, queryParams);
    }
}
