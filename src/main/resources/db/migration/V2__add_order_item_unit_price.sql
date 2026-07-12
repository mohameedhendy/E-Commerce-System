ALTER TABLE product_order_quantity
    ADD COLUMN unit_price NUMERIC(19, 2);

UPDATE product_order_quantity poq
SET unit_price = p.price
    FROM product p
WHERE p.id = poq.product_id;

ALTER TABLE product_order_quantity
    ALTER COLUMN unit_price SET NOT NULL;