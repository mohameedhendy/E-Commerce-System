ALTER TABLE web_order
    ADD COLUMN total_amount NUMERIC(19, 2);

UPDATE web_order wo
SET total_amount = COALESCE(
        (
            SELECT SUM(
                           poq.unit_price * poq.quantity
                   )
            FROM product_order_quantity poq
            WHERE poq.order_id = wo.id
        ),
        0.00
                   );

ALTER TABLE web_order
    ALTER COLUMN total_amount SET NOT NULL;