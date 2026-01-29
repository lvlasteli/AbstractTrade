package com.example.gateway.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class ErrorBuilderUtil {

    public static ResponseEntity<?> buildErrorResponse(ObjectMapper objectMapper, String serviceName, FeignException e) {
        String responseBody = e.contentUTF8();

        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                Map<String, Object> parsedResponse = objectMapper.readValue(
                        responseBody,
                        new TypeReference<Map<String, Object>>() {
                        }
                );
                return ResponseEntity.status(e.status()).body(parsedResponse);
            } catch (Exception ex) {
                log.warn("Failed to parse {} error response, returning as string: {}", serviceName, ex.getMessage());
            }
        }

        return getResponseEntity(e, responseBody);
    }

    @NonNull
    private static ResponseEntity<?> getResponseEntity(FeignException e, String responseBody) {
        Map<String, Object> errorResponse = new HashMap<String, Object>();
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

