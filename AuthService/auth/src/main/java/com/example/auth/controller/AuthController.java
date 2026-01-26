package com.example.auth.controller;

import com.example.auth.constants.ErrorMsg;
import com.example.auth.model.dto.request.LoginRequest;
import com.example.auth.model.dto.request.RegisterRequest;
import com.example.auth.model.dto.response.AuthResponse;
import com.example.auth.model.dto.response.ErrorResponse;
import com.example.auth.model.dto.response.SuccessResponse;
import com.example.auth.security.SessionCookieManager;
import com.example.auth.service.AuthenticationService;
import com.example.auth.service.RegistrationService;
import com.example.auth.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;
    private final SessionCookieManager sessionCookieManager;


    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = registrationService.register(request, RequestUtil.extractRequestMetadata(httpRequest));

        SuccessResponse<AuthResponse> successResponse = SuccessResponse.<AuthResponse>builder()
                .message("Registration successful")
                .data(response)
                .build();

        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String sessionId = authenticationService.login(request, RequestUtil.extractRequestMetadata(httpRequest));

        var sessionValidation = authenticationService.validateSession(sessionId);

        AuthResponse authData = AuthResponse.builder()
                .userId(sessionValidation.getUserId())
                .email(sessionValidation.getEmail())
                .roles(sessionValidation.getRoles())
                .build();

        SuccessResponse<AuthResponse> response = SuccessResponse.<AuthResponse>builder()
                .message("Login successful")
                .data(authData)
                .build();

        ResponseCookie cookie = sessionCookieManager.createSessionCookie(sessionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(HttpServletRequest httpRequest) {
        sessionCookieManager.extractSessionId(httpRequest).ifPresent(sessionId -> authenticationService.logout(sessionId, RequestUtil.extractRequestMetadata(httpRequest)));

        ResponseCookie cookie = sessionCookieManager.createLogoutCookie();

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .message("Logged out successfully")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest httpRequest) {
        String sessionId = sessionCookieManager.extractSessionId(httpRequest)
                .orElse(null);

        if (sessionId == null) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(ErrorMsg.NO_SESSION_FOUND)
                    .build();
            return ResponseEntity.status(401)
                    .body(errorResponse);
        }

        boolean refreshed = authenticationService.refreshSession(sessionId);

        if (!refreshed) {
            ResponseCookie cookie = sessionCookieManager.createLogoutCookie();
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(ErrorMsg.SESSION_EXPIRED_OR_INVALID)
                    .build();
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(errorResponse);
        }

        ResponseCookie cookie = sessionCookieManager.createSessionCookie(sessionId);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .message("Session refreshed")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
}
