package com.example.auth.util;

import com.example.auth.model.dto.request.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtil {

    private static String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    private static String extractDeviceInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }

        // Simple device detection based on User-Agent
        if (userAgent.contains("Mobile")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    public static RequestMetadata extractRequestMetadata(HttpServletRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);
        String deviceInfo = extractDeviceInfo(userAgent);
        return new RequestMetadata(ipAddress, userAgent, deviceInfo);
    }
}
