package com.example.cart.service;

import com.example.cart.model.dto.CartContext;
import com.example.cart.repository.RedisCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartContextFactory {

    private final RedisCartRepository cartRepository;

    public CartContext create(String cartId, String userId) {
        boolean anonymous = userId == null;
        String identifier = anonymous ? cartId : userId;

        return new CartContext(
                anonymous,
                identifier,
                anonymous
                        ? cartRepository.getAnonCartKey(identifier)
                        : cartRepository.getUserCartKey(identifier),
                anonymous
                        ? cartRepository.getAnonCartItemsKey(identifier)
                        : cartRepository.getUserCartItemsKey(identifier)
        );
    }
}
