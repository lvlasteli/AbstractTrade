package com.example.product.repository;

import com.example.product.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends PagingAndSortingRepository<Category, UUID> {
    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    List<Category> findAll();

    List<Category> findAllByOrderByNameAsc();
    
    @Query("SELECT c, COUNT(p.id) FROM Category c " +
           "LEFT JOIN Product p ON p.category.id = c.id AND p.isActive = true " +
           "GROUP BY c.id, c.name, c.description, c.slug, c.imageUrl, c.createdAt, c.updatedAt " +
           "ORDER BY c.name ASC")
    List<Object[]> findAllWithProductCountOrdered();
    
    @Query("SELECT c, COUNT(p.id) FROM Category c " +
           "LEFT JOIN Product p ON p.category.id = c.id AND p.isActive = true " +
           "GROUP BY c.id, c.name, c.description, c.slug, c.imageUrl, c.createdAt, c.updatedAt")
    List<Object[]> findAllWithProductCount();
    
    @Query(value = "SELECT c, COUNT(p.id) FROM Category c " +
           "LEFT JOIN Product p ON p.category.id = c.id AND p.isActive = true " +
           "GROUP BY c.id, c.name, c.description, c.slug, c.imageUrl, c.createdAt, c.updatedAt " +
           "ORDER BY c.name ASC",
           countQuery = "SELECT COUNT(DISTINCT c.id) FROM Category c")
    Page<Object[]> findAllWithProductCountOrdered(Pageable pageable);
}
