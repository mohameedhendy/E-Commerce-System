ALTER TABLE stock
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE stock
    ADD CONSTRAINT chk_stock_quantity_non_negative
        CHECK (quantity >= 0);