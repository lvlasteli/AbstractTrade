package com.example.auth.security;

import com.example.auth.config.CookieConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Manages session cookie creation and extraction.
 */
@Component
@RequiredArgsConstructor
public class SessionCookieManager {

    private final CookieConfig cookieConfig;

    /**
     * Creates a session cookie with the given session ID.
     *
     * @param sessionId The session ID
     * @return The ResponseCookie to be set in the response
     */
    public ResponseCookie createSessionCookie(String sessionId) {
        return ResponseCookie.from(cookieConfig.getName(), sessionId)
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .sameSite(cookieConfig.getSameSite())
                .path("/")
                .maxAge(Duration.ofSeconds(cookieConfig.getMaxAgeSeconds()))
                .build();
    }

    /**
     * Creates a cookie that clears the session (for logout).
     *
     * @return The ResponseCookie to clear the session
     */
    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(cookieConfig.getName(), "")
                .httpOnly(true)
                .secure(cookieConfig.isSecure())
                .sameSite(cookieConfig.getSameSite())
                .path("/")
                .maxAge(0)
                .build();
    }

    /**
     * Extracts the session ID from the request cookies.
     *
     * @param request The HTTP request
     * @return Optional containing the session ID if present
     */
    public Optional<String> extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieConfig.getName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    /**
     * Gets the cookie name.
     *
     * @return The cookie name
     */
    public String getCookieName() {
        return cookieConfig.getName();
    }
}
