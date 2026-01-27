package com.example.product.repository;

import com.example.product.model.entity.Category;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends PagingAndSortingRepository<Category, UUID> {
    Optional<Category> findById(UUID id);

    List<Category> findAll();

    List<Category> findAllByOrderByNameAsc();
}
