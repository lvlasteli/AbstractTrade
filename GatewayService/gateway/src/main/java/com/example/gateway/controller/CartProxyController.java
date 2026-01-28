package com.example.gateway.controller;

import com.example.gateway.client.CartServiceClient;
import com.example.gateway.model.CartIdentity;
import com.example.gateway.service.CartIdentityService;
import com.example.gateway.util.ErrorBuilderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartProxyController {
    private final CartServiceClient cartServiceClient;
    private final ObjectMapper objectMapper;
    private final CartIdentityService cartIdentityService;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String serviceName = "CartService";
        try {
            CartIdentity identity = cartIdentityService.resolveIdentity(request);
            if (identity.getSetCookie() != null && !identity.getSetCookie().isBlank()) {
                response.addHeader(HttpHeaders.SET_COOKIE, identity.getSetCookie());
            }

            String path = request.getRequestURI();
            String method = request.getMethod();
            
            Map<String, String> queryParams = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    queryParams.put(key, values[0]); // Take first value if multiple
                } else {
                    queryParams.put(key, "");
                }
            });
            
            log.debug("Proxying {} {} to {}", method, path, serviceName);
            
            ResponseEntity<Object> cartResponse = switch (method) {
                case "GET" -> cartServiceClient.forwardGetRequest(path, queryParams);
                case "POST" -> cartServiceClient.forwardPostRequest(path, queryParams, body);
                case "PUT" -> cartServiceClient.forwardPutRequest(path, queryParams, body);
                case "DELETE" -> cartServiceClient.forwardDeleteRequest(path, queryParams);
                default -> ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(Map.of("error", "Method not allowed", "method", method));
            };
            
            return cartResponse;
            
        } catch (FeignException e) {
            log.error("Error forwarding to {}: {} - Status: {}", serviceName, e.getMessage(), e.status());
            return ErrorBuilderUtil.buildErrorResponse(objectMapper, serviceName, e);
        }
    }
}
