ALTER TABLE product_order_quantity
    ADD COLUMN product_name VARCHAR(255);

UPDATE product_order_quantity poq
SET product_name = p.name
    FROM product p
WHERE p.id = poq.product_id;

ALTER TABLE product_order_quantity
    ALTER COLUMN product_name SET NOT NULL;