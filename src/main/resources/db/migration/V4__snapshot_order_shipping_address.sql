ALTER TABLE web_order
    ADD COLUMN shipping_address_line_1 VARCHAR(512),
    ADD COLUMN shipping_address_line_2 VARCHAR(512),
    ADD COLUMN shipping_country VARCHAR(75),
    ADD COLUMN shipping_city VARCHAR(255);

UPDATE web_order wo
SET shipping_address_line_1 = a.address_line_1,
    shipping_address_line_2 = a.address_line_2,
    shipping_country = a.country,
    shipping_city = a.city
    FROM address a
WHERE a.id = wo.address_id;

ALTER TABLE web_order
    ALTER COLUMN shipping_address_line_1 SET NOT NULL,
ALTER COLUMN shipping_country SET NOT NULL,
    ALTER COLUMN shipping_city SET NOT NULL;