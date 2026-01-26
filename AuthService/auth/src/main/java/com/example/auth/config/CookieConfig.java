package com.example.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.cookie")
@Getter
@Setter
public class CookieConfig {

    private String name = "auth_session_id";

    private int maxAgeSeconds = 86400; //24 hours

    /**
     * Whether the cookie should only be sent over HTTPS.
     */
    private boolean secure = true;

    private String sameSite = "Lax";
}
