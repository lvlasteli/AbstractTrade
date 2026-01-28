package com.example.cart.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.cookie")
@Getter
@Setter
public class CookieConfig {

    private String authSessionCookieName = "auth-session.name";

    private String anonCartCookieName = "anon-cart.name";

}