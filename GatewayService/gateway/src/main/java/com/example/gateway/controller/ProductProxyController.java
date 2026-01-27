package com.example.gateway.controller;

import com.example.gateway.client.ProductServiceClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductProxyController {

    @Value("${gateway.service.secret.header-name:X-Gateway-Request}")
    private String headerName;

    private final ProductServiceClient productServiceClient;
    private final ObjectMapper objectMapper;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        
        try {
            var header = request.getHeader(headerName);
            String path = request.getRequestURI();
            String method = request.getMethod();
            
            // Extract query parameters using Spring's built-in parsing
            Map<String, String> queryParams = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    queryParams.put(key, values[0]); // Take first value if multiple
                } else {
                    queryParams.put(key, "");
                }
            });
            
            log.debug("Proxying {} {} {} to ProductService", method, path, header);
            
            return switch (method) {
                case "GET" -> productServiceClient.forwardGetRequest(path, queryParams);
                case "POST" -> productServiceClient.forwardPostRequest(path, queryParams, body);
                case "PUT" -> productServiceClient.forwardPutRequest(path, queryParams, body);
                case "DELETE" -> productServiceClient.forwardDeleteRequest(path, queryParams);
                default -> ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(Map.of("error", "Method not allowed", "method", method));
            };
            
        } catch (FeignException e) {
            log.error("Error forwarding to ProductService: {} - Status: {}", e.getMessage(), e.status());
            return buildErrorResponse(e);
        }
    }
    
    private ResponseEntity<?> buildErrorResponse(FeignException e) {
        String responseBody = e.contentUTF8();
        
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                Map<String, Object> parsedResponse = objectMapper.readValue(
                    responseBody, 
                    new TypeReference<Map<String, Object>>() {}
                );
                return ResponseEntity.status(e.status()).body(parsedResponse);
            } catch (Exception ex) {
                log.warn("Failed to parse Product Service error response, returning as string: {}", ex.getMessage());
            }
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        
        try {
            errorResponse.put("status", e.status());
            HttpStatus httpStatus = HttpStatus.valueOf(e.status());
            errorResponse.put("error", httpStatus.getReasonPhrase());
        } catch (IllegalArgumentException ex) {
            errorResponse.put("error", "Error");
        }
        
        errorResponse.put("message", responseBody != null ? responseBody : e.getMessage());
        
        return ResponseEntity.status(e.status()).body(errorResponse);
    }
}
