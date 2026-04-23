package com.vidara.tradecenter.product.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.product.dto.request.CreateProductReviewRequest;
import com.vidara.tradecenter.product.dto.response.ProductReviewResponse;
import com.vidara.tradecenter.product.dto.response.ProductReviewSummaryResponse;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.model.ProductReview;
import com.vidara.tradecenter.product.model.enums.ProductStatus;
import com.vidara.tradecenter.product.repository.ProductRepository;
import com.vidara.tradecenter.product.repository.ProductReviewRepository;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductReviewService(ProductReviewRepository reviewRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProductReviewSummaryResponse listReviews(Long productId, Long currentUserIdOrNull) {
        ensureProductExists(productId);
        long count = reviewRepository.countByProductId(productId);
        Double avg = reviewRepository.averageRatingByProductId(productId);
        double average = avg != null
                ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        List<ProductReviewResponse> list = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(r -> toResponse(r, currentUserIdOrNull))
                .collect(Collectors.toList());

        return new ProductReviewSummaryResponse(average, count, list);
    }

    @Transactional(readOnly = true)
    public Optional<ProductReviewResponse> getMyReview(Long productId, Long userId) {
        ensureProductExists(productId);
        return reviewRepository.findByProductIdAndUserId(productId, userId)
                .map(r -> toResponse(r, userId));
    }

    @Transactional
    public ProductReviewResponse createOrUpdateReview(Long productId, Long userId, CreateProductReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Reviews are only allowed on active products");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ProductReview review = reviewRepository.findByProductIdAndUserId(productId, userId)
                .orElseGet(() -> {
                    ProductReview r = new ProductReview();
                    r.setProduct(product);
                    r.setUser(user);
                    return r;
                });

        review.setRating(request.getRating());
        review.setComment(request.getComment().trim());
        ProductReview saved = reviewRepository.save(review);
        return toResponse(saved, userId);
    }

    public void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
    }

    private ProductReviewResponse toResponse(ProductReview r, Long currentUserIdOrNull) {
        ProductReviewResponse dto = new ProductReviewResponse();
        dto.setId(r.getId());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setReviewerDisplayName(displayName(r.getUser()));
        dto.setMine(currentUserIdOrNull != null && r.getUser().getId().equals(currentUserIdOrNull));
        return dto;
    }

    private static String displayName(User user) {
        if (user == null) {
            return "Customer";
        }
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        if (!first.isEmpty() && !last.isEmpty()) {
            return first + " " + last.charAt(0) + ".";
        }
        if (!first.isEmpty()) {
            return first;
        }
        return "Customer";
    }
}
