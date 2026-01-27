package com.example.gateway.constants;

public final class ErrorMsg {
    private ErrorMsg() {
    }
    
    // Error codes
    public static final String IP_BLOCKED = "IP_BLOCKED";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    
    // Error messages
    public static final String TOO_MANY_REGISTRATION_ATTEMPTS = "Too many registration attempts. Please try again later.";
    public static final String IP_TEMPORARILY_BLOCKED = "IP temporarily blocked. Try again later.";
    public static final String TOO_MANY_LOGIN_ATTEMPTS_FROM_IP = "Too many login attempts from this IP. Please try again later.";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";
}
