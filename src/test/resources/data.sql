-- Passwords:
-- UserA = PasswordA123
-- UserB = PasswordB123
-- UserC = PasswordC123

INSERT INTO local_user
(email,
 first_name,
 last_name,
 password,
 username,
 email_verified,
 role,
 password_reset_version)
VALUES ('UserA@junit.com',
        'UserA-FirstName',
        'UserA-LastName',
        '$2a$10$s6u9/a9H6arpRiLeYXt9re/Uescc3qvqzV/mgN5W1L/gCO1OYBbD2',
        'UserA',
        true,
        'USER',
        0),
       ('UserB@junit.com',
        'UserB-FirstName',
        'UserB-LastName',
        '$2a$10$U8U3WGo7MmKaVb..wHlYze9EvsjdhfKn8zlVIccVb8Sr4ImvQ5kC2',
        'UserB',
        false,
        'USER',
        0),
       ('UserC@junit.com',
        'UserC-FirstName',
        'UserC-LastName',
        '$2a$10$u9s.85Xsm0EdwAh1E8QdZOj7mqQxZeDM6vxIFBxzn0Bd.oObkzj3K',
        'UserC',
        false,
        'USER',
        0);

INSERT INTO address
    (address_line_1, city, country, user_id)
VALUES ('123 Tester Hill', 'Testerton', 'England', 1),
       ('312 Spring Boot', 'Hibernate', 'England', 3);

INSERT INTO product
    (name, short_description, long_description, price, active)
VALUES ('Product #1',
        'Product one short description.',
        'This is a very long description of product #1.',
        5.50,
        true),
       ('Product #2',
        'Product two short description.',
        'This is a very long description of product #2.',
        10.56,
        true),
       ('Product #3',
        'Product three short description.',
        'This is a very long description of product #3.',
        2.74,
        true),
       ('Product #4',
        'Product four short description.',
        'This is a very long description of product #4.',
        15.69,
        true),
       ('Product #5',
        'Product five short description.',
        'This is a very long description of product #5.',
        42.59,
        true);

INSERT INTO stock
    (product_id, quantity, version)
VALUES (1, 5, 0),
       (2, 8, 0),
       (3, 12, 0),
       (4, 73, 0),
       (5, 2, 0);

INSERT INTO web_order
(address_id,
 user_id,
 status,
 created_at,
 total_amount,
 shipping_address_line_1,
 shipping_address_line_2,
 shipping_country,
 shipping_city)
VALUES (1,
        1,
        'PENDING',
        CURRENT_TIMESTAMP,
        80.30,
        '123 Tester Hill',
        NULL,
        'England',
        'Testerton'),
       (1,
        1,
        'PENDING',
        CURRENT_TIMESTAMP,
        279.45,
        '123 Tester Hill',
        NULL,
        'England',
        'Testerton'),
       (1,
        1,
        'PENDING',
        CURRENT_TIMESTAMP,
        13.70,
        '123 Tester Hill',
        NULL,
        'England',
        'Testerton'),
       (2,
        3,
        'PENDING',
        CURRENT_TIMESTAMP,
        131.25,
        '312 Spring Boot',
        NULL,
        'England',
        'Hibernate'),
       (2,
        3,
        'PENDING',
        CURRENT_TIMESTAMP,
        41.20,
        '312 Spring Boot',
        NULL,
        'England',
        'Hibernate');

INSERT INTO product_order_quantity
(
    order_id,
    product_id,
    quantity,
    unit_price,
    product_name
)
VALUES
    (1, 1, 5, 5.50, 'Product #1'),
    (1, 2, 5, 10.56, 'Product #2'),
    (2, 3, 5, 2.74, 'Product #3'),
    (2, 2, 5, 10.56, 'Product #2'),
    (2, 5, 5, 42.59, 'Product #5'),
    (3, 3, 5, 2.74, 'Product #3'),
    (4, 4, 5, 15.69, 'Product #4'),
    (4, 2, 5, 10.56, 'Product #2'),
    (5, 3, 5, 2.74, 'Product #3'),
    (5, 1, 5, 5.50, 'Product #1');
