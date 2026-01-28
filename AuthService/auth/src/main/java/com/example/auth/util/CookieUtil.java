package com.example.auth.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class CookieUtil {

    public static ResponseCookie createCookie(String cookieName, String value,
    boolean isSecure, String sameSite, int maxAgeSec) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSec))
                .build();
    }
}
