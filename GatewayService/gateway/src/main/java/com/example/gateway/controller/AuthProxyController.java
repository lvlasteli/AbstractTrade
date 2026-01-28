package com.example.gateway.controller;

import com.example.gateway.service.proxy.AuthProxyAdapter;
import com.example.gateway.service.proxy.ProxyForwarderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthProxyController {

    private final ProxyForwarderService proxyForwarderService;
    private final AuthProxyAdapter authProxyAdapter;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        return proxyForwarderService.forward(" AuthService", authProxyAdapter, request, body);
    }
}
