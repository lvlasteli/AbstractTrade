package com.example.product.constants;

public final class ErrorMsg {

    private ErrorMsg() {
    }

    public static final String VALIDATION_FAILED = "Validation Failed";
    public static final String NOT_FOUND = "Not Found";
    public static final String INVALID_PAGINATION = "Invalid pagination parameters";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String REDIS_UNAVAILABLE = "Cache service temporarily unavailable";

    public static final String GATEWAY_ACCESS_DENIED = "Access Denied";
    public static final String GATEWAY_ACCESS_DENIED_MESSAGE = "Access denied. Request must originate from gateway service.";
}
