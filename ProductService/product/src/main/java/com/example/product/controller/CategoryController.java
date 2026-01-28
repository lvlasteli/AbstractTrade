package com.example.product.controller;

import com.example.product.model.dto.request.PaginationRequest;
import com.example.product.model.dto.response.CategoryResponse;
import com.example.product.model.dto.response.PageResponse;
import com.example.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
            @Valid @ModelAttribute PaginationRequest pagination) {
        
        int page = pagination.getPage() != null ? pagination.getPage() : 0;
        int size = pagination.getSize() != null ? pagination.getSize() : 20;
        
        PageResponse<CategoryResponse> response = categoryService.getAllCategoriesPaginated(page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @PathVariable String slug,
            @Valid @ModelAttribute PaginationRequest pagination) {
        
        CategoryResponse category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }
}
