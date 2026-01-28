package com.example.product.exception;

import com.example.product.constants.ErrorMsg;
import com.example.product.model.dto.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ErrorMsg.VALIDATION_FAILED)
                .errors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ErrorMsg.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        String message = ex.getMessage();
        if (message != null && (message.contains("page") || message.contains("size"))) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorMsg.INVALID_PAGINATION, 
                    ErrorMsg.INVALID_PAGINATION + ": " + message);
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorMsg.VALIDATION_FAILED, message);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<ErrorResponse> handleRedisConnectionFailure(RedisConnectionFailureException ex) {
        log.warn("Redis connection failure, falling back to database: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ErrorMsg.REDIS_UNAVAILABLE,
                "Cache service temporarily unavailable, but service is still operational");
    }

    @ExceptionHandler(GatewayAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleGatewayAccessDenied(GatewayAccessDeniedException ex) {
        log.warn("Gateway access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ErrorMsg.GATEWAY_ACCESS_DENIED,
                ErrorMsg.GATEWAY_ACCESS_DENIED_MESSAGE);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMsg.INTERNAL_SERVER_ERROR,
                ErrorMsg.UNEXPECTED_ERROR);
    }
}
