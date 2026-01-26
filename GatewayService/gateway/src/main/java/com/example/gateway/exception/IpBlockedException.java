package com.example.gateway.exception;


public class IpBlockedException extends RuntimeException {

    public IpBlockedException(String message) {
        super(message);
    }
}
