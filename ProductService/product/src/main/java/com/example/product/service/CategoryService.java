package com.example.product.service;

import com.example.product.model.dto.response.CategoryResponse;
import com.example.product.model.dto.response.PageResponse;
import com.example.product.model.dto.response.ProductResponse;
import com.example.product.model.entity.Category;
import com.example.product.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final CategoryCacheService categoryCacheService;
    
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
            .map(this::mapToCategoryResponse)
            .collect(Collectors.toList());
    }
    
    public List<CategoryResponse> getAllCategoriesUnordered() {
        return categoryRepository.findAll().stream()
            .map(this::mapToCategoryResponse)
            .collect(Collectors.toList());
    }
    
    public CategoryResponse getCategoryById(UUID id) {
        Optional<CategoryResponse> cachedCategoryOpt = categoryCacheService.getCachedCategory(id);
        
        if (cachedCategoryOpt.isPresent()) {
            CategoryResponse cachedCategory = cachedCategoryOpt.get();
            log.debug("Category {} found in cache", id);
            Optional<PageResponse<ProductResponse>> productsOpt = productService.getProductsByCategoryId(id, 0, 20);
            if (productsOpt.isPresent()) {
                cachedCategory.setProductCount(productsOpt.get().getTotalElements());
            } else {
                cachedCategory.setProductCount(0L);
            }
            return cachedCategory;
        }
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        CategoryResponse categoryResponse = mapToCategoryResponse(category);
        
        Optional<PageResponse<ProductResponse>> productsOpt = productService.getProductsByCategoryId(id, 0, 20);
        if (productsOpt.isPresent()) {
            categoryResponse.setProductCount(productsOpt.get().getTotalElements());
        } else {
            categoryResponse.setProductCount(0L);
        }
        
        categoryCacheService.cacheCategory(id, categoryResponse);
        
        return categoryResponse;
    }
    
    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
            .id(category.getId())
            .name(category.getName())
            .description(category.getDescription())
            .slug(category.getSlug())
            .imageUrl(category.getImageUrl())
            .createdAt(category.getCreatedAt())
            .build();
    }
}
