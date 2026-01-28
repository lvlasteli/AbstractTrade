package com.example.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.cookie")
@Getter
@Setter
public class CookieConfig {

    private AuthSession authSession = new AuthSession();
    private AnonCart anonCart = new AnonCart();

    @Getter
    @Setter
    public static class AuthSession {
        private String name = "auth_session_id";
    }

    @Getter
    @Setter
    public static class AnonCart {
        private String name = "anon_cart_id";
    }
}
