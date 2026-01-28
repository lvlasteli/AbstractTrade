package com.example.auth.controller;

import com.example.auth.model.dto.response.AnonCartResponse;
import com.example.auth.model.dto.response.SuccessResponse;
import com.example.auth.security.AnonCartCookieManager;
import com.example.auth.service.AnonCartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth/anon-cart")
@RequiredArgsConstructor
@Slf4j
public class AnonCartController {

    private final AnonCartService anonCartService;

    private final AnonCartCookieManager anonCartCookieManager;

    @GetMapping("/generate")
    public ResponseEntity<SuccessResponse<AnonCartResponse>> generateCartId() {
        String cartId = anonCartService.generateCartId();

        AnonCartResponse cartResponse = AnonCartResponse.builder()
                .cartId(cartId)
                .build();

        SuccessResponse<AnonCartResponse> response = SuccessResponse.<AnonCartResponse>builder()
                .message("Anonymous cart ID generated successfully")
                .data(cartResponse)
                .build();

        ResponseCookie cookie = anonCartCookieManager.createAnonCartCookie(cartId);

        log.debug("Generated anonymous cart ID: {}", cartId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<Void>> refreshAnonCartCookie(HttpServletRequest request) {
        ResponseCookie refreshedCookie = anonCartCookieManager.refreshAnonCartCookie(request);
        
        if (refreshedCookie == null) {
            SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                    .message("Anonymous cart cookie not found")
                    .build();
            return ResponseEntity.ok().body(response);
        }

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .message("Anonymous cart cookie refreshed successfully")
                .build();

        log.debug("Refreshed anonymous cart cookie");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshedCookie.toString())
                .body(response);
    }
}
