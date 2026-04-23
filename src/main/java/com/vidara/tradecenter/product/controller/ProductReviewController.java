package com.vidara.tradecenter.product.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.product.dto.request.CreateProductReviewRequest;
import com.vidara.tradecenter.product.dto.response.ProductReviewResponse;
import com.vidara.tradecenter.product.dto.response.ProductReviewSummaryResponse;
import com.vidara.tradecenter.product.service.ProductReviewService;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    public ProductReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductReviewSummaryResponse>> list(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        ProductReviewSummaryResponse data = productReviewService.listReviews(productId, userId);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", data));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProductReviewResponse>> myReview(
            @PathVariable Long productId,
            @CurrentUser CustomUserDetails currentUser) {
        Optional<ProductReviewResponse> mine = productReviewService.getMyReview(productId, currentUser.getId());
        return mine.map(r -> ResponseEntity.ok(ApiResponse.success("Your review", r)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success("No review yet", null)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductReviewResponse>> submit(
            @PathVariable Long productId,
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody CreateProductReviewRequest request) {
        ProductReviewResponse saved = productReviewService.createOrUpdateReview(
                productId, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Review saved", saved));
    }
}
