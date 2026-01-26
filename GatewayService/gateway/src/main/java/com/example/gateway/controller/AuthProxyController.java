package com.example.gateway.controller;

import com.example.gateway.client.AuthServiceClient;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthProxyController {

    private final AuthServiceClient authServiceClient;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            log.debug("Proxying {} {} to AuthService", method, path);
            
            return switch (method) {
                case "GET" -> authServiceClient.forwardGetRequest(path);
                case "POST" -> authServiceClient.forwardPostRequest(path, body);
                case "PUT" -> authServiceClient.forwardPutRequest(path, body);
                case "DELETE" -> authServiceClient.forwardDeleteRequest(path);
                default -> ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(Map.of("error", "Method not allowed", "method", method));
            };
            
        } catch (FeignException e) {
            log.error("Error forwarding to AuthService: {} - Status: {}", e.getMessage(), e.status());
            return buildErrorResponse(e);
        }
    }
    
    private ResponseEntity<Map<String, Object>> buildErrorResponse(FeignException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", e.status());
        
        try {
            HttpStatus httpStatus = HttpStatus.valueOf(e.status());
            errorResponse.put("error", httpStatus.getReasonPhrase());
        } catch (IllegalArgumentException ex) {
            errorResponse.put("error", "Error");
        }
        
        String responseBody = e.contentUTF8();
        if (responseBody != null && !responseBody.isEmpty()) {
            errorResponse.put("message", responseBody);
        } else {
            errorResponse.put("message", e.getMessage());
        }
        
        return ResponseEntity.status(e.status()).body(errorResponse);
    }
}
