package com.example.cart.util;

import com.example.cart.config.CookieConfig;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class CookieExtractor {

    private final CookieConfig cookieConfig;

    public CookieExtractor(CookieConfig cookieConfig) {
        this.cookieConfig = cookieConfig;
    }


    public Optional<String> extractUserId(HttpServletRequest request) {
        return extractFromHeader("X-User-Id", request)
                .or(() -> extractCookie(request, cookieConfig.getAuthSessionCookieName()));
    }

    public Optional<String> extractCartId(HttpServletRequest request) {
        return extractFromHeader("X-Cart-Id", request)
                .or(() -> extractCookie(request, cookieConfig.getAnonCartCookieName()));
    }

    private Optional<String> extractFromHeader(String headerKey, HttpServletRequest request) {
        String userId = request.getHeader(headerKey);
        return Optional.ofNullable(userId)
                .filter(id -> !id.isBlank());
    }

    private Optional<String> extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }


    public Optional<String> extractAnonCartIdFromHeader(String cookieHeader) {
        if (StringUtils.isBlank(cookieHeader)) {
            return Optional.empty();
        }

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && cookieConfig.getAnonCartCookieName().equals(parts[0].trim())) {
                String value = parts[1].trim();
                if (!value.isBlank()) {
                    return Optional.of(value);
                }
            }
        }

        return Optional.empty();
    }
}
