package com.vidara.tradecenter.product.dto.response;

import java.util.List;

public class ProductReviewSummaryResponse {

    private double averageRating;
    private long reviewCount;
    private List<ProductReviewResponse> reviews;

    public ProductReviewSummaryResponse() {
    }

    public ProductReviewSummaryResponse(double averageRating, long reviewCount, List<ProductReviewResponse> reviews) {
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.reviews = reviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<ProductReviewResponse> getReviews() {
        return reviews;
    }

    public void setReviews(List<ProductReviewResponse> reviews) {
        this.reviews = reviews;
    }
}
