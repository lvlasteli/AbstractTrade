package com.example.product.service;

import com.example.product.model.dto.response.PageResponse;
import com.example.product.model.dto.response.ProductResponse;
import com.example.product.model.entity.Category;
import com.example.product.model.entity.Product;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public Optional<ProductResponse> getProductById(UUID id) {
        return productRepository.findById(id)
            .map(this::mapToProductResponse);
    }   

    public PageResponse<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByIsActive(true, pageable)
            .orElse(Page.empty(pageable));
        
        return buildPageResponse(productPage);
    }

    public Optional<PageResponse<ProductResponse>> searchProducts(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            query, query, pageable
        ).map(this::buildPageResponse);
    }
    
    public Optional<PageResponse<ProductResponse>> getProductsByCategoryId(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryIdAndIsActive(categoryId, true, pageable)
            .map(this::buildPageResponse);
    }

    private PageResponse<ProductResponse> buildPageResponse(Page<Product> productPage) {
        return PageResponse.<ProductResponse>builder()
            .content(productPage.getContent().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList()))
            .page(productPage.getNumber())
            .size(productPage.getSize())
            .totalElements(productPage.getTotalElements())
            .totalPages(productPage.getTotalPages())
            .hasNext(productPage.hasNext())
            .hasPrevious(productPage.hasPrevious())
            .build();
    }

    private ProductResponse mapToProductResponse(Product product) {
        Category category = product.getCategory();
        UUID categoryId = product.getCategoryId() != null ? product.getCategoryId() :
                         (category != null ? category.getId() : null);
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .sku(product.getSku())
            .stock(product.getStock())
            .imageUrl(product.getImageUrl())
            .categoryId(categoryId)
            .categoryName(category != null ? category.getName() : null)
            .isActive(product.getIsActive())
            .createdAt(product.getCreatedAt())
            .build();
    }
}
