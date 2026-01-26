package com.example.auth.controller;

import com.example.auth.model.dto.request.PasswordForgotRequest;
import com.example.auth.model.dto.request.PasswordResetRequest;
import com.example.auth.model.dto.response.SuccessResponse;
import com.example.auth.service.PasswordRecoveryService;
import com.example.auth.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {

    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/forgot")
    public ResponseEntity<SuccessResponse<Void>> forgotPassword(
            @Valid @RequestBody PasswordForgotRequest request,
            HttpServletRequest httpRequest) {

        passwordRecoveryService.initiatePasswordReset(request, RequestUtil.extractRequestMetadata(httpRequest));

        // Always return success to prevent email enumeration
        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .message("Password reset link has been sent")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {


        passwordRecoveryService.resetPassword(request, RequestUtil.extractRequestMetadata(httpRequest));

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .message("Password has been reset successfully. Check the email")
                .build();

        return ResponseEntity.ok(response);
    }
}
