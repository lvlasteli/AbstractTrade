package com.example.product.service;

import com.example.product.model.dto.response.CategoryResponse;
import com.example.product.model.dto.response.PageResponse;
import com.example.product.model.dto.response.ProductResponse;
import com.example.product.model.entity.Category;
import com.example.product.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
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
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final CategoryCacheService categoryCacheService;
    
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllWithProductCountOrdered().stream()
            .map(result -> {
                Category category = (Category) result[0];
                Long productCount = ((Number) result[1]).longValue();
                CategoryResponse response = mapToCategoryResponse(category);
                response.setProductCount(productCount);
                return response;
            })
            .collect(Collectors.toList());
    }
    
    public PageResponse<CategoryResponse> getAllCategoriesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> categoryPage = categoryRepository.findAllWithProductCountOrdered(pageable);
        
        return PageResponse.<CategoryResponse>builder()
            .content(categoryPage.getContent().stream()
                .map(result -> {
                    Category category = (Category) result[0];
                    Long productCount = ((Number) result[1]).longValue();
                    CategoryResponse response = mapToCategoryResponse(category);
                    response.setProductCount(productCount);
                    return response;
                })
                .collect(Collectors.toList()))
            .page(categoryPage.getNumber())
            .size(categoryPage.getSize())
            .totalElements(categoryPage.getTotalElements())
            .totalPages(categoryPage.getTotalPages())
            .hasNext(categoryPage.hasNext())
            .hasPrevious(categoryPage.hasPrevious())
            .build();
    }

    
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with slug: " + slug));
        
        UUID categoryId = category.getId();
        Optional<CategoryResponse> cachedCategoryOpt = categoryCacheService.getCachedCategory(categoryId);
        
        if (cachedCategoryOpt.isPresent()) {
            CategoryResponse cachedCategory = cachedCategoryOpt.get();
            log.debug("Category {} found in cache", categoryId);
            Optional<PageResponse<ProductResponse>> productsOpt = productService.getProductsByCategoryId(categoryId, 0, 20);
            if (productsOpt.isPresent()) {
                cachedCategory.setProductCount(productsOpt.get().getTotalElements());
            } else {
                cachedCategory.setProductCount(0L);
            }
            return cachedCategory;
        }
        
        CategoryResponse categoryResponse = mapToCategoryResponse(category);
        
        Optional<PageResponse<ProductResponse>> productsOpt = productService.getProductsByCategoryId(categoryId, 0, 20);
        if (productsOpt.isPresent()) {
            categoryResponse.setProductCount(productsOpt.get().getTotalElements());
        } else {
            categoryResponse.setProductCount(0L);
        }
        
        categoryCacheService.cacheCategory(categoryId, categoryResponse);
        
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
