package com.example.cart.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cart")
@Getter
@Setter
public class CartProperties {
    private int maxItemsPerCart = 100;
    private int maxQuantityPerSku = 999;
    private long anonTtlSeconds = 259200;
}