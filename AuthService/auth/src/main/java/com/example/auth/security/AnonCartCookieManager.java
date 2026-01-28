package com.example.auth.security;

import com.example.auth.config.AnonCartCookieConfig;
import com.example.auth.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AnonCartCookieManager {

    private final AnonCartCookieConfig anonCartCookieConfig;

    public ResponseCookie createAnonCartCookie(String cartId) {
        return CookieUtil.createCookie(anonCartCookieConfig.getName(), cartId, anonCartCookieConfig.isSecure(),
                anonCartCookieConfig.getSameSite(), anonCartCookieConfig.getMaxAgeSeconds());
    }
}
