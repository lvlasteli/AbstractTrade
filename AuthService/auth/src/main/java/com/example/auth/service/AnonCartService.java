package com.example.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AnonCartService {

    public String generateCartId() {
        String cartId = UUID.randomUUID().toString();
        log.debug("Generated anonymous cart ID: {}", cartId);
        return cartId;
    }
}
