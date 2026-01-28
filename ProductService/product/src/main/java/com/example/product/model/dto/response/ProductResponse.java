package com.example.product.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private Integer stock;
    private String imageUrl;
    private UUID categoryId;
    private String categoryName;
    private Boolean isActive;
    private Instant createdAt;
}
