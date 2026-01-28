package com.example.product.exception;

public class GatewayAccessDeniedException extends RuntimeException {

    public GatewayAccessDeniedException(String message) {
        super(message);
    }
}
