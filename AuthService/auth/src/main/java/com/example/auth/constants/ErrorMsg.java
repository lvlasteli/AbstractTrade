package com.example.auth.constants;

public final class ErrorMsg {

    private ErrorMsg() {
    }

    public static final String VALIDATION_FAILED = "Validation Failed";

    public static final String AUTHENTICATION_FAILED = "Authentication Failed";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String INVALID_OR_EXPIRED_RESET_TOKEN = "Invalid or expired reset token";

    public static final String ACCOUNT_LOCKED = "Account Locked";
    public static final String ACCOUNT_IS_LOCKED = "Account is locked. Please try again later.";
    public static final String ACCOUNT_LOCKED_TOO_MANY_ATTEMPTS = "Account is locked due to too many failed attempts. Try again later.";

    public static final String SESSION_INVALID = "Session Invalid";
    public static final String SESSION_NOT_FOUND = "Session not found";
    public static final String NO_SESSION_FOUND = "No session found";
    public static final String SESSION_EXPIRED_OR_INVALID = "Session expired or invalid";

    public static final String ALREADY_EXISTS = "Already Exists";
    public static final String EMAIL_ALREADY_REGISTERED = "Email already registered: %s";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String DEFAULT_ROLE_NOT_FOUND = "Default role not found: %s";

    public static final String RATE_LIMIT_EXCEEDED = "Rate Limit Exceeded";
    public static final String TOO_MANY_PASSWORD_RESET_REQUESTS = "Too many password reset requests. Please try again later.";

    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";

    public static final String GATEWAY_ACCESS_DENIED = "Access Denied";
    public static final String GATEWAY_ACCESS_DENIED_MESSAGE = "Access denied. Request must originate from gateway service.";
}
