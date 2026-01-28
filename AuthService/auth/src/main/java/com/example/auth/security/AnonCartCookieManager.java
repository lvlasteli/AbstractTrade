package com.example.auth.security;

import com.example.auth.config.AnonCartCookieConfig;
import com.example.auth.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnonCartCookieManager {

    private final AnonCartCookieConfig anonCartCookieConfig;

    public ResponseCookie createAnonCartCookie(String cartId) {
        return CookieUtil.createCookie(anonCartCookieConfig.getName(), cartId, anonCartCookieConfig.isSecure(),
                anonCartCookieConfig.getSameSite(), anonCartCookieConfig.getMaxAgeSeconds());
    }

    public ResponseCookie refreshAnonCartCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (anonCartCookieConfig.getName().equals(cookie.getName())) {
                    ResponseCookie refreshedCookie = CookieUtil.createCookie(
                            anonCartCookieConfig.getName(),
                            cookie.getValue(),
                            anonCartCookieConfig.isSecure(),
                            anonCartCookieConfig.getSameSite(),
                            anonCartCookieConfig.getMaxAgeSeconds()
                    );
                    return refreshedCookie;
                }
            }
        }
        return null;
    }
}
