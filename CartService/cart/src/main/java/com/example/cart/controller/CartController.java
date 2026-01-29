package com.example.cart.controller;

import com.example.cart.model.dto.AddItemRequest;
import com.example.cart.model.dto.CartResponse;
import com.example.cart.service.CartService;
import com.example.cart.util.CookieExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final CookieExtractor cookieExtractor;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        String cartId = extractCartId(request);
        String userId = extractUserId(request);
        
        CartResponse cart = cartService.getCart(cartId, userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddItemRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Cart service received request: sku={}, quantity={}, quantityType={}", 
            request.getSku(), request.getQuantity(), 
            request.getQuantity() != null ? request.getQuantity().getClass().getName() : "null");
        
        String cartId = extractCartId(httpRequest);
        String userId = extractUserId(httpRequest);
        
        CartResponse cart = cartService.addItem(cartId, userId, request);
        
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.CREATED);
        if (userId == null && cartId != null) {
            responseBuilder.header("X-Cart-Cookie-Refresh", "true");
        }
        
        return responseBuilder.body(cart);
    }

    @PutMapping("/items/{sku}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable String sku,
            @RequestParam Integer quantity,
            @RequestParam(required = false) Integer version,
            HttpServletRequest request) {
        
        String cartId = extractCartId(request);
        String userId = extractUserId(request);
        
        CartResponse cart = cartService.updateItemQuantity(cartId, userId, sku, quantity, version);
        
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        if (userId == null && cartId != null) {
            responseBuilder.header("X-Cart-Cookie-Refresh", "true");
        }
        
        return responseBuilder.body(cart);
    }

    @DeleteMapping("/items/{sku}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable String sku,
            @RequestParam(required = false) Integer version,
            HttpServletRequest request) {
        
        String cartId = extractCartId(request);
        String userId = extractUserId(request);
        
        CartResponse cart = cartService.updateItemQuantity(cartId, userId, sku, 0, version);
        
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        if (userId == null && cartId != null) {
            responseBuilder.header("X-Cart-Cookie-Refresh", "true");
        }
        
        return responseBuilder.body(cart);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCart(HttpServletRequest request) {
        String cartId = extractCartId(request);
        String userId = extractUserId(request);
        
        cartService.deleteCart(cartId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponse> clearCart(HttpServletRequest request) {
        String cartId = extractCartId(request);
        String userId = extractUserId(request);
        
        CartResponse cart = cartService.clearCart(cartId, userId);
        
        // Signal cookie refresh for anonymous carts on write operations
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        if (userId == null && cartId != null) {
            responseBuilder.header("X-Cart-Cookie-Refresh", "true");
        }
        
        return responseBuilder.body(cart);
    }

    private String extractCartId(HttpServletRequest request) {
        return cookieExtractor.extractCartId(request)
                .orElse(null);
    }

    private String extractUserId(HttpServletRequest request) {
        return cookieExtractor.extractUserId(request)
                .orElse(null);
    }
}
