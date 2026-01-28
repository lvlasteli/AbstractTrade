package com.example.gateway.controller;

import com.example.gateway.client.AuthServiceClient;
import com.example.gateway.util.ErrorBuilderUtil;
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
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthProxyController {

    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        String serviceName = " AuthService";
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            log.debug("Proxying {} {} to {}", method, path,serviceName);
            
            return switch (method) {
                case "GET" -> authServiceClient.forwardGetRequest(path);
                case "POST" -> authServiceClient.forwardPostRequest(path, body);
                case "PUT" -> authServiceClient.forwardPutRequest(path, body);
                case "DELETE" -> authServiceClient.forwardDeleteRequest(path);
                default -> ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(Map.of("error", "Method not allowed", "method", method));
            };
            
        } catch (FeignException e) {
            log.error("Error forwarding to {}: {} - Status: {}", serviceName, e.getMessage(), e.status());
            return ErrorBuilderUtil.buildErrorResponse(objectMapper, serviceName, e);
        }
    }
}
