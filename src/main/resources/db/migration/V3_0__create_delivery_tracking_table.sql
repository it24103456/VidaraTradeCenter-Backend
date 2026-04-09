CREATE TABLE IF NOT EXISTS delivery_tracking (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PREPARING',
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    tracking_number VARCHAR(100),
    courier_name VARCHAR(100),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_delivery_order ON delivery_tracking(order_id);
CREATE INDEX IF NOT EXISTS idx_delivery_status ON delivery_tracking(status);
CREATE INDEX IF NOT EXISTS idx_delivery_tracking_number ON delivery_tracking(tracking_number);
CREATE INDEX IF NOT EXISTS idx_delivery_estimated_date ON delivery_tracking(estimated_delivery_date);

-- Comment on table
COMMENT ON TABLE delivery_tracking IS 'Tracks delivery status and shipping information for orders';
COMMENT ON COLUMN delivery_tracking.status IS 'PREPARING, SHIPPED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, RETURNED, FAILED';