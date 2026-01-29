package com.example.auth.security;

import com.example.auth.config.CookieConfig;
import com.example.auth.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionCookieManager {

    private final CookieConfig cookieConfig;

    public ResponseCookie createSessionCookie(String sessionId) {
        return CookieUtil.createCookie(cookieConfig.getName(), sessionId, cookieConfig.isSecure(),
                cookieConfig.getSameSite(), cookieConfig.getMaxAgeSeconds());
    }

    public ResponseCookie createLogoutCookie() {
        return CookieUtil.createCookie(cookieConfig.getName(), "", cookieConfig.isSecure(),
                cookieConfig.getSameSite(), 0);
    }

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

}
