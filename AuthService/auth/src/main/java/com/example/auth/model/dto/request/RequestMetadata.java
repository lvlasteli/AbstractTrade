package com.example.auth.model.dto.request;

/**
 * Record to hold HTTP request metadata (IP address, User-Agent, and device info).
 * Used for audit logging and security tracking.
 */
public record RequestMetadata(
        String ipAddress,
        String userAgent,
        String deviceInfo
        // TO-DO: device fingerprint?
) {
}
