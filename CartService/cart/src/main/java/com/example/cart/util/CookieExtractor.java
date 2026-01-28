package com.example.cart.util;

import com.example.cart.config.CookieConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class CookieExtractor {

    private CookieConfig cookieConfig;

    public Optional<String> extractAnonCartId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieConfig.getAnonCartCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    public Optional<String> extractAnonCartIdFromHeader(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) {
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
