package com.example.gateway.controller;

import com.example.gateway.client.CartServiceClient;
import com.example.gateway.model.CartIdentity;
import com.example.gateway.model.dto.request.AddCartItemRequest;
import com.example.gateway.service.CartIdentityService;
import com.example.gateway.service.ProductValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final ProductValidationService productValidationService;
    private final CartServiceClient cartServiceClient;
    private final CartIdentityService cartIdentityService;
    
    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(
            @Valid @RequestBody AddCartItemRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        log.info("Add item to cart request: sku={}, quantity={}", request.getSku(), request.getQuantity());
        productValidationService.fetchProductBySku(request.getSku(), request.getQuantity());

        CartIdentity identity = cartIdentityService.resolveIdentity(httpRequest);
        log.debug("Resolved cart identity: userId={}, cartId={}", identity.getUserId(), identity.getCartId());
        
        // Forward the request directly to preserve type information
        log.debug("Forwarding to cart service: sku={}, quantity={}", request.getSku(), request.getQuantity());
        
        ResponseEntity<Object> cartResponse = cartServiceClient.forwardPostRequest(
            "/cart/items", 
            null, 
            request
        );
        
        log.info("Item added to cart successfully: sku={}, quantity={}", request.getSku(), request.getQuantity());
        
        if (identity.getSetCookie() != null && !identity.getSetCookie().isBlank()) {
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, identity.getSetCookie());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(cartResponse.getBody());
    }
}
