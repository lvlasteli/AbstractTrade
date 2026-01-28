package com.example.gateway.service.proxy;

import com.example.gateway.util.ErrorBuilderUtil;
import com.example.gateway.util.QueryParamExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyForwarderService {

    private final ObjectMapper objectMapper;

    public ResponseEntity<?> forward(
            String serviceName,
            ProxyAdapter adapter,
            HttpServletRequest request,
            Object body) {
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            Map<String, String> queryParams = QueryParamExtractor.extract(request);

            log.debug("Proxying {} {} to {}", method, path, serviceName);

            return switch (method) {
                case "GET" -> adapter.get(path, queryParams);
                case "POST" -> adapter.post(path, queryParams, body);
                case "PUT" -> adapter.put(path, queryParams, body);
                case "DELETE" -> adapter.delete(path, queryParams);
                default -> ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(Map.of("error", "Method not allowed", "method", method));
            };

        } catch (FeignException e) {
            log.error("Error forwarding to {}: {} - Status: {}", serviceName, e.getMessage(), e.status());
            return ErrorBuilderUtil.buildErrorResponse(objectMapper, serviceName, e);
        }
    }
}
