package com.example.cart.exception;

public class CartVersionConflictException extends RuntimeException {
    private final Integer currentVersion;

    public CartVersionConflictException(String message, Integer currentVersion) {
        super(message);
        this.currentVersion = currentVersion;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }
}
