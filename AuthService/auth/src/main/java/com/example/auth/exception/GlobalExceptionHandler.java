package com.example.auth.exception;

import com.example.auth.constants.ErrorMsg;
import com.example.auth.exception.GatewayAccessDeniedException;
import com.example.auth.model.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ErrorMsg.AUTHENTICATION_FAILED, ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ErrorMsg.ACCOUNT_LOCKED, ex.getMessage());
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ErrorMsg.SESSION_INVALID, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ErrorMsg.ALREADY_EXISTS, ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ErrorMsg.RATE_LIMIT_EXCEEDED, ex.getMessage());
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
        log.error("Exception: {} - {}", ex.getMessage(), ex.getStackTrace());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMsg.INTERNAL_SERVER_ERROR,
                ErrorMsg.UNEXPECTED_ERROR);
    }
}
