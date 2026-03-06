package com.vidara.tradecenter.product.repository;

import com.vidara.tradecenter.product.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    // Find brand by slug
    Optional<Brand> findBySlug(String slug);

    // Check if brand name already exists
    Boolean existsByName(String name);

    // Check if slug already exists
    Boolean existsBySlug(String slug);
}
