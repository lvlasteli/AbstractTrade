package com.example.gateway.util;

import jakarta.servlet.http.HttpServletRequest;

public final class IpExtractor {

    private IpExtractor() {
    }

    public static String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
