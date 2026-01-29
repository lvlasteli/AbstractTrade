package com.example.cart.exception;

public class CartValidationException extends RuntimeException {
    public CartValidationException(String message) {
        super(message);
    }
}
