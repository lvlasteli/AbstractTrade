package com.example.cart.service;

import com.example.cart.config.CartProperties;
import com.example.cart.exception.CartNotFoundException;
import com.example.cart.exception.CartValidationException;
import com.example.cart.exception.CartVersionConflictException;
import com.example.cart.model.CartItem;
import com.example.cart.model.CartMetadata;
import com.example.cart.model.dto.AddItemRequest;
import com.example.cart.model.dto.CartContext;
import com.example.cart.model.dto.CartResponse;
import com.example.cart.repository.RedisCartRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisCartRepository cartRepository;
    private final LuaScriptService luaScriptService;
    private final CartProperties properties;
    private final CartContextFactory cartContextFactory;

    public CartResponse addItem(String cartId, String userId, AddItemRequest request) {
        CartContext ctx = cartContextFactory.create(cartId, userId);

        Map<Object, Object> currentItems = cartRepository.getCartItems(ctx.itemsKey());
        if (currentItems.size() >= properties.getMaxItemsPerCart() && !currentItems.containsKey("sku:" + request.getSku())) {
            throw new CartValidationException("Cart cannot contain more than " + properties.getMaxItemsPerCart() + " items");
        }

        List<String> keys = List.of(ctx.cartKey(), ctx.itemsKey());
        List<String> args = List.of(
                request.getSku(),
                String.valueOf(request.getQuantity()),
                ctx.anonymous() ? "1" : "0",
                String.valueOf(properties.getAnonTtlSeconds()),
                "EUR",
                "eu-west-1"
        );

        log.info("Executing Lua script - keys: {}, args: {}", keys, args);
        log.info("Quantity value: {}, type: {}, toString: '{}'", 
            request.getQuantity(), 
            request.getQuantity().getClass().getName(),
            String.valueOf(request.getQuantity()));

        try {
            Integer newVersion = luaScriptService.executeAddItem(keys, args);
            log.debug("Added item {} to cart {}, new version: {}", request.getSku(), ctx.identifier(), newVersion);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("MAX_QUANTITY_EXCEEDED")) {
                throw new CartValidationException("Quantity exceeds maximum allowed (" + properties.getMaxQuantityPerSku() + ")");
            }
            if (e.getMessage().contains("INVALID_QUANTITY")) {
                throw new CartValidationException("Invalid quantity provided");
            }
            if (e.getMessage().contains("INVALID_CURRENT_QUANTITY")) {
                throw new CartValidationException("Invalid current quantity in cart");
            }
            throw e;
        }

        return getCart(cartId, userId);
    }

    public CartResponse getCart(String cartId, String userId) {
        CartContext ctx = cartContextFactory.create(cartId, userId);

        if (!cartRepository.exists(ctx.cartKey())) {
            throw new CartNotFoundException("Cart not found");
        }

        Map<Object, Object> metadataMap = cartRepository.getCartMetadata(ctx.cartKey());
        CartMetadata metadata = mapToCartMetadata(metadataMap);

        Map<Object, Object> itemsMap = cartRepository.getCartItems(ctx.itemsKey());
        List<CartItem> items = mapToCartItems(itemsMap);

        BigDecimal subtotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(ctx.identifier())
                .items(items)
                .itemCount(items.size())
                .subtotal(subtotal)
                .currency(metadata.getCurrency())
                .version(metadata.getVersion())
                .build();
    }

    public CartResponse updateItemQuantity(String cartId, String userId, String sku, Integer quantity, Integer expectedVersion) {
        boolean isAnonymous = userId == null;
        String identifier = isAnonymous ? cartId : userId;

        if (quantity < 0) {
            throw new CartValidationException("Quantity cannot be negative");
        }
        if (quantity > properties.getMaxQuantityPerSku()) {
            throw new CartValidationException("Quantity exceeds maximum allowed (" + properties.getMaxQuantityPerSku() + ")");
        }

        String cartKey = isAnonymous
                ? cartRepository.getAnonCartKey(identifier)
                : cartRepository.getUserCartKey(identifier);
        String itemsKey = isAnonymous
                ? cartRepository.getAnonCartItemsKey(identifier)
                : cartRepository.getUserCartItemsKey(identifier);

        if (!cartRepository.exists(cartKey)) {
            throw new CartNotFoundException("Cart not found");
        }

        List<String> keys = List.of(cartKey, itemsKey);
        List<String> args = new ArrayList<>(List.of(
                sku,
                String.valueOf(quantity),
                isAnonymous ? "1" : "0",
                String.valueOf(properties.getAnonTtlSeconds())
        ));

        if (expectedVersion != null) {
            args.add(String.valueOf(expectedVersion));
        }

        try {
            Integer newVersion = luaScriptService.executeUpdateItem(keys, args);
            log.debug("Updated item {} in cart {}, new version: {}", sku, identifier, newVersion);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("VERSION_MISMATCH")) {
                String[] parts = e.getMessage().split(":");
                Integer currentVersion = parts.length > 1 ? Integer.parseInt(parts[1]) : null;
                throw new CartVersionConflictException("Cart version mismatch", currentVersion);
            }
            if (e.getMessage().contains("CART_NOT_FOUND")) {
                throw new CartNotFoundException("Cart not found");
            }
            if (e.getMessage().contains("MAX_QUANTITY_EXCEEDED")) {
                throw new CartValidationException("Quantity exceeds maximum allowed (" + properties.getMaxQuantityPerSku() + ")");
            }
            if (e.getMessage().contains("INVALID_QUANTITY")) {
                throw new CartValidationException("Invalid quantity provided");
            }
            throw e;
        }

        return getCart(cartId, userId);
    }

    public void deleteCart(String cartId, String userId) {
        boolean isAnonymous = userId == null;
        String identifier = isAnonymous ? cartId : userId;

        String cartKey = isAnonymous
                ? cartRepository.getAnonCartKey(identifier)
                : cartRepository.getUserCartKey(identifier);
        String itemsKey = isAnonymous
                ? cartRepository.getAnonCartItemsKey(identifier)
                : cartRepository.getUserCartItemsKey(identifier);

        if (!cartRepository.exists(cartKey)) {
            throw new CartNotFoundException("Cart not found");
        }

        cartRepository.deleteCart(cartKey, itemsKey);
        log.debug("Deleted cart: {}", identifier);
    }

    public CartResponse clearCart(String cartId, String userId) {
        boolean isAnonymous = userId == null;
        String identifier = isAnonymous ? cartId : userId;

        String cartKey = isAnonymous
                ? cartRepository.getAnonCartKey(identifier)
                : cartRepository.getUserCartKey(identifier);
        String itemsKey = isAnonymous
                ? cartRepository.getAnonCartItemsKey(identifier)
                : cartRepository.getUserCartItemsKey(identifier);

        if (!cartRepository.exists(cartKey)) {
            throw new CartNotFoundException("Cart not found");
        }

        Map<Object, Object> itemsMap = cartRepository.getCartItems(itemsKey);
        for (Object itemKey : itemsMap.keySet()) {
            String sku = itemKey.toString().replace("sku:", "");
            updateItemQuantity(cartId, userId, sku, 0, null);
        }

        return getCart(cartId, userId);
    }

    private CartMetadata mapToCartMetadata(Map<Object, Object> metadataMap) {
        if (metadataMap == null || metadataMap.isEmpty()) {
            return CartMetadata.builder()
                    .currency("USD")
                    .status("ACTIVE")
                    .version(1)
                    .region("us-east-1")
                    .build();
        }

        String currency = getStringValue(metadataMap, "currency", "USD");
        String createdAtStr = getStringValue(metadataMap, "created_at", null);
        String updatedAtStr = getStringValue(metadataMap, "updated_at", null);
        String status = getStringValue(metadataMap, "status", "ACTIVE");
        String versionStr = getStringValue(metadataMap, "version", "1");
        String region = getStringValue(metadataMap, "region", "us-east-1");

        Instant createdAt = parseTimestamp(createdAtStr);
        Instant updatedAt = parseTimestamp(updatedAtStr);
        Integer version = Integer.parseInt(versionStr);

        return CartMetadata.builder()
                .currency(currency)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .status(status)
                .version(version)
                .region(region)
                .build();
    }

    private List<CartItem> mapToCartItems(Map<Object, Object> itemsMap) {
        if (itemsMap == null || itemsMap.isEmpty()) {
            return new ArrayList<>();
        }

        return itemsMap.entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith("sku:"))
                .map(entry -> {
                    String sku = entry.getKey().toString().replace("sku:", "");
                    int quantity = Integer.parseInt(entry.getValue().toString());
                    BigDecimal unitPrice = BigDecimal.ZERO;
                    BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

                    return CartItem.builder()
                            .sku(sku)
                            .quantity(quantity)
                            .unitPrice(unitPrice)
                            .totalPrice(totalPrice)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getStringValue(Map<Object, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private Instant parseTimestamp(String timestampStr) {
        if (timestampStr == null) {
            return Instant.now();
        }
        try {
            if (timestampStr.contains(".")) {
                String[] parts = timestampStr.split("\\.");
                long seconds = Long.parseLong(parts[0]);
                long nanos = parts.length > 1 ? Long.parseLong(parts[1]) * 1000 : 0;
                return Instant.ofEpochSecond(seconds, nanos);
            } else {
                return Instant.ofEpochSecond(Long.parseLong(timestampStr));
            }
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}", timestampStr, e);
            return Instant.now();
        }
    }
}
