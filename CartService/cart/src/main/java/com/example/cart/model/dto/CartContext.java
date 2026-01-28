package com.example.cart.model.dto;

public record CartContext(
        boolean anonymous,
        String identifier,
        String cartKey,
        String itemsKey
) {}