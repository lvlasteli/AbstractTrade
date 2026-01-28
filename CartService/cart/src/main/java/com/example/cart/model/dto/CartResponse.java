package com.example.cart.model.dto;

import com.example.cart.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private String cartId;
    private List<CartItem> items;
    private Integer itemCount;
    private BigDecimal subtotal;
    private String currency;
    private Integer version;
}
