package com.example.product.service;

import com.example.product.exception.ProductNotAvailableException;
import com.example.product.exception.ProductNotFoundException;
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

import java.util.List;
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
    
    public Optional<ProductResponse> getProductBySku(String sku) {
        return productRepository.findBySkuAndIsActive(sku, true)
            .map(this::mapToProductResponse);
    }
    
    /**
     * Get product by SKU with validation for cart operations.
     * Allows out-of-stock products as users can wait for restocking.
     * 
     * @param sku The product SKU
     * @param quantity Optional quantity parameter (not used for validation, kept for API compatibility)
     * @return ProductResponse
     * @throws ProductNotFoundException if product doesn't exist
     * @throws ProductNotAvailableException if product is not active
     */
    public ProductResponse getProductBySkuWithValidation(String sku, Integer quantity) {
        log.debug("Validating product: sku={}", sku);
        
        // Find product by SKU (includes inactive products for validation)
        Product product = productRepository.findBySku(sku)
            .orElseThrow(() -> new ProductNotFoundException(
                String.format("Product not found with SKU: %s", sku)
            ));
        
        // Validate product is active
        if (product.getIsActive() == null || !product.getIsActive()) {
            log.warn("Product is not active: sku={}", sku);
            throw new ProductNotAvailableException(
                String.format("Product with SKU %s is not available", sku)
            );
        }
        
        log.info("Product validation successful: sku={}", sku);
        return mapToProductResponse(product);
    }   

    public PageResponse<ProductResponse> getAllProducts(int page, int size) {
        long totalElements = productRepository.countByIsActive(true);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        if (totalElements > 0 && page >= totalPages) {
            page = totalPages - 1;
            log.warn("Requested page {} is out of bounds. Adjusted to last page: {}", page + totalPages, page);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByIsActive(true, pageable)
            .orElse(Page.empty(pageable));
        
        return buildPageResponse(productPage);
    }

    public PageResponse<ProductResponse> searchProducts(String query, int page, int size) {
        long totalElements = productRepository.countByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        if (totalElements > 0 && page >= totalPages) {
            page = totalPages - 1;
            log.warn("Requested page {} is out of bounds for search '{}'. Adjusted to last page: {}", page + totalPages, query, page);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            query, query, pageable
        ).map(this::buildPageResponse)
        .orElse(PageResponse.<ProductResponse>builder()
            .content(List.of())
            .page(page)
            .size(size)
            .totalElements(0)
            .totalPages(0)
            .hasNext(false)
            .hasPrevious(false)
            .build());
    }
    
    public Optional<PageResponse<ProductResponse>> getProductsByCategoryId(UUID categoryId, int page, int size) {
        long totalElements = productRepository.countByCategoryIdAndIsActive(categoryId, true);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        if (totalElements > 0 && page >= totalPages) {
            page = totalPages - 1;
            log.warn("Requested page {} is out of bounds for category {}. Adjusted to last page: {}", page + totalPages, categoryId, page);
        }
        
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
