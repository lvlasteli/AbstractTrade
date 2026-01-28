package com.example.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartMetadata {
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;
    private String status;
    private Integer version;
    private String region;
}
