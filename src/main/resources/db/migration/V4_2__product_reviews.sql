CREATE TABLE IF NOT EXISTS product_reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    rating SMALLINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_product_review_user UNIQUE (product_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_product_reviews_product ON product_reviews (product_id);
CREATE INDEX IF NOT EXISTS idx_product_reviews_user ON product_reviews (user_id);
