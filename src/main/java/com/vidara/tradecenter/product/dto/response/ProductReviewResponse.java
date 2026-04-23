package com.vidara.tradecenter.product.dto.response;

import java.time.LocalDateTime;

public class ProductReviewResponse {

    private Long id;
    private int rating;
    private String comment;
    private String reviewerDisplayName;
    private LocalDateTime createdAt;
    private boolean mine;

    public ProductReviewResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReviewerDisplayName() {
        return reviewerDisplayName;
    }

    public void setReviewerDisplayName(String reviewerDisplayName) {
        this.reviewerDisplayName = reviewerDisplayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
}
