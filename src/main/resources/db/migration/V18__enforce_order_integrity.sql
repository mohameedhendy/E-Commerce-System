ALTER TABLE product_order_quantity
    ADD CONSTRAINT uk_order_item_order_product
        UNIQUE (order_id, product_id);

ALTER TABLE product_order_quantity
    ADD CONSTRAINT chk_order_item_quantity_positive
        CHECK (quantity > 0);

ALTER TABLE product_order_quantity
    ADD CONSTRAINT chk_order_item_unit_price_non_negative
        CHECK (unit_price >= 0);

ALTER TABLE web_order
    ADD CONSTRAINT chk_web_order_total_amount_non_negative
        CHECK (total_amount >= 0);

ALTER TABLE product
    ADD CONSTRAINT chk_product_price_positive
        CHECK (price > 0);