package com.example.gateway.controller;

import com.example.gateway.service.proxy.ProductProxyAdapter;
import com.example.gateway.service.proxy.ProxyForwarderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductProxyController {

    @Value("${gateway.service.secret.header-name:X-Gateway-Request}")
    private String headerName;

    private final ProxyForwarderService proxyForwarderService;
    private final ProductProxyAdapter productProxyAdapter;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRequest(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request) {
        return proxyForwarderService.forward(" ProductService", productProxyAdapter, request, body);
    }
}
