package com.vidara.tradecenter.product.repository;

import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.model.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // Find products by status with pagination
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // Find products by category with pagination
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Find products by brand with pagination
    Page<Product> findByBrandId(Long brandId, Pageable pageable);

    // Find product by slug
    Optional<Product> findBySlug(String slug);

    // Find product by SKU
    Optional<Product> findBySku(String sku);

    // Check if SKU already exists
    Boolean existsBySku(String sku);

    // Search products by name or description
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Find products with low stock (stock <= lowStockThreshold)
    @Query("SELECT p FROM Product p WHERE p.stock IS NOT NULL AND p.lowStockThreshold IS NOT NULL " +
            "AND p.stock > 0 AND p.stock <= p.lowStockThreshold")
    Page<Product> findLowStockProducts(Pageable pageable);

    // Find out of stock products (stock = 0 or null)
    @Query("SELECT p FROM Product p WHERE p.stock IS NULL OR p.stock = 0")
    Page<Product> findOutOfStockProducts(Pageable pageable);

    // Find products by status and stock availability
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.stock > :minStock")
    Page<Product> findByStatusAndStockGreaterThan(@Param("status") ProductStatus status,
            @Param("minStock") Integer minStock,
            Pageable pageable);

    // Count low stock products
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock IS NOT NULL AND p.lowStockThreshold IS NOT NULL " +
            "AND p.stock > 0 AND p.stock <= p.lowStockThreshold")
    Long countLowStockProducts();

    // Count out of stock products
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stock IS NULL OR p.stock = 0")
    Long countOutOfStockProducts();
}
