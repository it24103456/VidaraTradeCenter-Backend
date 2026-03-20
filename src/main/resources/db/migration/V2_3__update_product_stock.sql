ALTER TABLE products
    ADD COLUMN IF NOT EXISTS stock INTEGER DEFAULT 0 CHECK (stock >= 0);

CREATE INDEX IF NOT EXISTS idx_product_stock ON products(stock);