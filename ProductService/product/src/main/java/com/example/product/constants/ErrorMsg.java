package com.example.product.constants;

public final class ErrorMsg {

    private ErrorMsg() {
    }

    public static final String VALIDATION_FAILED = "Validation Failed";
    public static final String NOT_FOUND = "Not Found";
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String CATEGORY_NOT_FOUND = "Category not found";
    public static final String INVALID_PAGINATION = "Invalid pagination parameters";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";
    public static final String REDIS_UNAVAILABLE = "Cache service temporarily unavailable";
}
