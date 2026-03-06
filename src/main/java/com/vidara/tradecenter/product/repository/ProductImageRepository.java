package com.vidara.tradecenter.product.repository;

import com.vidara.tradecenter.product.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Find all images for a product
    List<ProductImage> findByProductId(Long productId);
}
