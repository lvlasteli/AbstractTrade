package com.example.auth.controller;

import com.example.auth.constants.ErrorMsg;
import com.example.auth.model.dto.response.ErrorResponse;
import com.example.auth.model.dto.response.SessionValidationResponse;
import com.example.auth.model.dto.response.SuccessResponse;
import com.example.auth.model.dto.response.UserInfoResponse;
import com.example.auth.security.SessionCookieManager;
import com.example.auth.service.AuthenticationService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth/session")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final AuthenticationService authenticationService;
    private final SessionCookieManager sessionCookieManager;

    @GetMapping("/validate")
    public ResponseEntity<SuccessResponse<SessionValidationResponse>> validateSession(
            @RequestHeader(value = "X-Session-Id", required = false) String gatewaySessionId,
            HttpServletRequest request) {

        String sessionId = null;
        if (StringUtils.isEmpty(gatewaySessionId)) {
            sessionId = sessionCookieManager.extractSessionId(request).orElse(null);
        }

        SessionValidationResponse validationData;
        if (StringUtils.isEmpty(sessionId)) {
            validationData = SessionValidationResponse.builder()
                    .valid(false)
                    .build();
        } else {
            validationData = authenticationService.validateSession(sessionId);
        }

        SuccessResponse<SessionValidationResponse> response = SuccessResponse.<SessionValidationResponse>builder()
                .message("Session validation completed")
                .data(validationData)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String sessionId = sessionCookieManager.extractSessionId(request)
                .orElse(null);

        if (sessionId == null) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(ErrorMsg.NO_SESSION_FOUND)
                    .build();
            return ResponseEntity.status(401).body(errorResponse);
        }

        try {
            UserInfoResponse userInfo = authenticationService.getCurrentUser(sessionId);
            SuccessResponse<UserInfoResponse> response = SuccessResponse.<UserInfoResponse>builder()
                    .message("User information retrieved successfully")
                    .data(userInfo)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.debug("Failed to get current user: {} for sessionId: {}", e.getMessage(), sessionId);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error(ErrorMsg.SESSION_INVALID)
                    .build();
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
}
