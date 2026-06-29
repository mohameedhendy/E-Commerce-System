-- PostgreSQL dummy orders data
-- Replace these values with your real user id and address id

DO $$
    DECLARE
        v_user_id BIGINT := 3;
        v_address_id BIGINT := 1;

        v_order_1 BIGINT;
        v_order_2 BIGINT;

        v_product_1 BIGINT;
        v_product_2 BIGINT;
        v_product_3 BIGINT;
    BEGIN
        SELECT id INTO v_product_1 FROM product WHERE name = 'Product #1';
        SELECT id INTO v_product_2 FROM product WHERE name = 'Product #2';
        SELECT id INTO v_product_3 FROM product WHERE name = 'Product #3';

        INSERT INTO web_order (user_id, address_id)
        VALUES (v_user_id, v_address_id)
        RETURNING id INTO v_order_1;

        INSERT INTO web_order (user_id, address_id)
        VALUES (v_user_id, v_address_id)
        RETURNING id INTO v_order_2;

        INSERT INTO product_order_quantity (order_id, product_id, quantity)
        VALUES
            (v_order_1, v_product_1, 2),
            (v_order_1, v_product_2, 1),
            (v_order_2, v_product_3, 3);
    END $$;