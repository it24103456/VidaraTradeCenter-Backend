package com.vidara.tradecenter.product.repository;

import com.vidara.tradecenter.product.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdOrderByCreatedAtDesc(Long productId);

    Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);

    long countByProductId(Long productId);

    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
    Double averageRatingByProductId(@Param("productId") Long productId);
}
