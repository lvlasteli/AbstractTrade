package com.example.product.repository;

import com.example.product.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<Product, UUID> , CrudRepository<Product, UUID> {
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = :isActive")
    Optional<Page<Product>> findByCategoryIdAndIsActive(@Param("categoryId") UUID categoryId, @Param("isActive") Boolean isActive, Pageable pageable);

    Optional<Page<Product>> findByIsActive(Boolean isActive, Pageable pageable);

    Optional<Page<Product>> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, 
        String description, 
        Pageable pageable
    );

    Optional<Product> findBySku(String sku);
    
    Optional<Product> findBySkuAndIsActive(String sku, Boolean isActive);
    
    long countByIsActive(Boolean isActive);
    
    long countByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.isActive = :isActive")
    long countByCategoryIdAndIsActive(@Param("categoryId") UUID categoryId, @Param("isActive") Boolean isActive);
}
