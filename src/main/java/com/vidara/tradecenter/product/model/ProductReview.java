package com.vidara.tradecenter.product.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.user.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "product_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_review_user", columnNames = {"product_id", "user_id"})
}, indexes = {
        @Index(name = "idx_product_reviews_product", columnList = "product_id"),
        @Index(name = "idx_product_reviews_user", columnList = "user_id")
})
public class ProductReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
