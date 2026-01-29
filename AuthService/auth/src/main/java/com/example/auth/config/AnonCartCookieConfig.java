package com.example.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.cookie.anon-cart")
@Getter
@Setter
public class AnonCartCookieConfig {

    private String name = "anon_cart_id";

    private int maxAgeSeconds = 259200; // 72 hours

    private boolean secure = true;

    private String sameSite = "Lax";
}
