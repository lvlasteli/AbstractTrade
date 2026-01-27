package com.example.product.controller;

import com.example.product.model.dto.request.PaginationRequest;
import com.example.product.model.dto.response.CategoryResponse;
import com.example.product.model.dto.response.PageResponse;
import com.example.product.model.dto.response.ProductResponse;
import com.example.product.service.CategoryService;
import com.example.product.service.ProductService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Valid @ModelAttribute PaginationRequest pagination) {
        
        // Apply defaults if null (when query params not provided)
        int page = pagination.getPage() != null ? pagination.getPage() : 0;
        int size = pagination.getSize() != null ? pagination.getSize() : 20;
        
        PageResponse<ProductResponse> response = productService.getAllProducts(page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        ProductResponse product = productService.getProductById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam String q,
            @Valid @ModelAttribute PaginationRequest pagination) {
        
        // Apply defaults if null (when query params not provided)
        int page = pagination.getPage() != null ? pagination.getPage() : 0;
        int size = pagination.getSize() != null ? pagination.getSize() : 20;
        
        PageResponse<ProductResponse> response = productService.searchProducts(q, page, size)
            .orElse(PageResponse.<ProductResponse>builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable UUID id,
            @Valid @ModelAttribute PaginationRequest pagination) {
        
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
}
