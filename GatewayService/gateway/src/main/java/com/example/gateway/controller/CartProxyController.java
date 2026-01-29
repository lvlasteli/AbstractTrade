package com.example.gateway.controller;

import com.example.gateway.model.CartIdentity;
import com.example.gateway.service.CartIdentityService;
import com.example.gateway.service.proxy.CartProxyAdapter;
import com.example.gateway.service.proxy.ProxyForwarderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartProxyController {
    private final ProxyForwarderService proxyForwarderService;
    private final CartProxyAdapter cartProxyAdapter;
    private final CartIdentityService cartIdentityService;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Handle cart identity resolution and cookie setting (cart-specific logic)
        CartIdentity identity = cartIdentityService.resolveIdentity(request);
        if (identity.getSetCookie() != null && !identity.getSetCookie().isBlank()) {
            response.addHeader(HttpHeaders.SET_COOKIE, identity.getSetCookie());
        }

        return proxyForwarderService.forward("CartService", cartProxyAdapter, request, body);
    }
}
