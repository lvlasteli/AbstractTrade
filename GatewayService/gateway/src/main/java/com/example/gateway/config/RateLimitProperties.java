package com.example.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private Login login = new Login();
    private Register register = new Register();
    private CartCreation cartCreation = new CartCreation();

    @Getter
    @Setter
    public static class Login {
        private Ip ip = new Ip();

        @Getter
        @Setter
        public static class Ip {
            private int maxAttempts = 10;
            private int windowMinutes = 15;
        }
    }

    @Getter
    @Setter
    public static class Register {
        private Ip ip = new Ip();

        @Getter
        @Setter
        public static class Ip {
            private int maxAttempts = 5;
            private int windowMinutes = 60;
        }
    }

    @Getter
    @Setter
    public static class CartCreation {
        private Ip ip = new Ip();

        @Getter
        @Setter
        public static class Ip {
            private int maxAttempts = 3;
            private int windowHours = 1;
        }
    }
}
