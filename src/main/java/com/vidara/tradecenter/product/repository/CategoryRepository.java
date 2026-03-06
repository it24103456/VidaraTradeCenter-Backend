package com.vidara.tradecenter.product.repository;

import com.vidara.tradecenter.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find all root categories (no parent)
    List<Category> findByParentCategoryIsNull();

    // Find category by slug
    Optional<Category> findBySlug(String slug);

    // Check if category name already exists
    Boolean existsByName(String name);

    // Check if slug already exists
    Boolean existsBySlug(String slug);
}
