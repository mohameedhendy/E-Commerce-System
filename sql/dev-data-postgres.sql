-- PostgreSQL dummy dev data for products and stock

INSERT INTO product (name, short_description, long_description, price)
VALUES
    ('Product #1', 'Product one short description.', 'This is a very long description of product #1.', 5.50),
    ('Product #2', 'Product two short description.', 'This is a very long description of product #2.', 10.56),
    ('Product #3', 'Product three short description.', 'This is a very long description of product #3.', 2.74),
    ('Product #4', 'Product four short description.', 'This is a very long description of product #4.', 15.69),
    ('Product #5', 'Product five short description.', 'This is a very long description of product #5.', 42.59)
ON CONFLICT (name)
    DO UPDATE SET
                  short_description = EXCLUDED.short_description,
                  long_description = EXCLUDED.long_description,
                  price = EXCLUDED.price;


INSERT INTO stock (product_id, quantity)
SELECT p.id, v.quantity
FROM (
         VALUES
             ('Product #1', 5),
             ('Product #2', 8),
             ('Product #3', 12),
             ('Product #4', 73),
             ('Product #5', 2)
     ) AS v(product_name, quantity)
         JOIN product p ON p.name = v.product_name
ON CONFLICT (product_id)
    DO UPDATE SET
    quantity = EXCLUDED.quantity;