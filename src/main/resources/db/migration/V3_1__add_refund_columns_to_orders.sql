-- Add refund-related columns to orders table
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS refund_amount DECIMAL(10, 2),
ADD COLUMN IF NOT EXISTS refund_reason VARCHAR(500),
ADD COLUMN IF NOT EXISTS refund_date TIMESTAMP,
ADD COLUMN IF NOT EXISTS refunded_by BIGINT;

-- Add foreign key constraint for refunded_by
ALTER TABLE orders
ADD CONSTRAINT fk_refunded_by
FOREIGN KEY (refunded_by) REFERENCES users(id) ON DELETE SET NULL;

-- Index for refund queries
CREATE INDEX IF NOT EXISTS idx_order_refund_date ON orders(refund_date);
CREATE INDEX IF NOT EXISTS idx_order_refunded_by ON orders(refunded_by);

-- Comment on columns
COMMENT ON COLUMN orders.refund_amount IS 'Amount refunded to customer (can be partial)';
COMMENT ON COLUMN orders.refund_reason IS 'Reason for the refund provided by admin';
COMMENT ON COLUMN orders.refund_date IS 'Timestamp when refund was processed';
COMMENT ON COLUMN orders.refunded_by IS 'Admin user ID who processed the refund';