package com.example.auth.exception;

public class GatewayAccessDeniedException extends RuntimeException {

    public GatewayAccessDeniedException(String message) {
        super(message);
    }
}
