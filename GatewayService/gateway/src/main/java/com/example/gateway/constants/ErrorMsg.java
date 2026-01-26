package com.example.gateway.constants;

public final class ErrorMsg {
    private ErrorMsg() {
    }
    public static final String TOO_MANY_REGISTRATION_ATTEMPTS = "Too many registration attempts. Please try again later.";
    public static final String IP_TEMPORARILY_BLOCKED = "IP temporarily blocked. Try again later.";
    public static final String TOO_MANY_LOGIN_ATTEMPTS_FROM_IP = "Too many login attempts from this IP. Please try again later.";
}
