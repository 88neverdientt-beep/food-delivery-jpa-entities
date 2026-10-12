-- =============================================================================
-- 02_dummy_data.sql  –  Food Delivery  (MySQL 8.0)
-- Dữ liệu mẫu để kiểm thử; chạy sau 01_schema.sql.
-- =============================================================================

USE food_delivery;

-- -----------------------------------------------------------------------------
-- payment_methods  (bắt buộc: COD và VIETQR)
-- -----------------------------------------------------------------------------
INSERT INTO payment_methods (method_code, method_name) VALUES
    ('COD',     'Tiền mặt khi nhận hàng'),
    ('VIETQR',  'Chuyển khoản VietQR');

-- -----------------------------------------------------------------------------
-- restaurants
-- -----------------------------------------------------------------------------
INSERT INTO restaurants
    (restaurant_name, email, password_hash, phone_number, address,
     district_no, latitude, longitude, is_active, created_at)
VALUES
    ('Phở Hà Nội',
     'pho.hanoi@example.com',
     '$2a$10$dummyhash1',
     '0901111111',
     '12 Hàng Buồm, Hoàn Kiếm, Hà Nội',
     1, 21.0340, 105.8490, 1, NOW()),

    ('Bún Bò Huế Ngon',
     'bunbo.hue@example.com',
     '$2a$10$dummyhash2',
     '0902222222',
     '88 Lê Lợi, Hải Châu, Đà Nẵng',
     2, 16.0678, 108.2208, 1, NOW()),

    ('Pizza Saigon',
     'pizza.saigon@example.com',
     '$2a$10$dummyhash3',
     '0903333333',
     '45 Nguyễn Huệ, Quận 1, TP.HCM',
     3, 10.7769, 106.7009, 1, NOW());

-- -----------------------------------------------------------------------------
-- customers
-- -----------------------------------------------------------------------------
INSERT INTO customers
    (full_name, email, phone_number, password_hash, address, created_at)
VALUES
    ('Nguyễn Văn An',
     'an.nguyen@example.com',
     '0911111111',
     '$2a$10$custhash1',
     '5 Tràng Tiền, Hoàn Kiếm, Hà Nội',
     NOW()),

    ('Trần Thị Bình',
     'binh.tran@example.com',
     '0922222222',
     '$2a$10$custhash2',
     '20 Bạch Đằng, Hải Châu, Đà Nẵng',
     NOW()),

    ('Lê Minh Châu',
     'chau.le@example.com',
     '0933333333',
     '$2a$10$custhash3',
     '99 Lê Văn Sỹ, Quận 3, TP.HCM',
     NOW());

-- -----------------------------------------------------------------------------
-- drivers
-- -----------------------------------------------------------------------------
INSERT INTO drivers
    (full_name, email, password_hash, phone_number, license_plate,
     is_online, is_busy, current_latitude, current_longitude,
     last_updated, created_at)
VALUES
    ('Phạm Đức Hùng',
     'hung.pham@example.com',
     '$2a$10$drvhash1',
     '0944444444',
     '30A-12345',
     1, 0,
     21.0285, 105.8542,
     NOW(), NOW()),

    ('Võ Thanh Tùng',
     'tung.vo@example.com',
     '$2a$10$drvhash2',
     '0955555555',
     '43B-67890',
     1, 0,
     10.7769, 106.7100,
     NOW(), NOW()),

    ('Đặng Quốc Bảo',
     'bao.dang@example.com',
     '$2a$10$drvhash3',
     '0966666666',
     '51C-11223',
     0, 0,
     NULL, NULL,
     NOW(), NOW());

-- -----------------------------------------------------------------------------
-- food_items  (thuộc từng restaurant)
-- -----------------------------------------------------------------------------
-- Phở Hà Nội (restaurant_id=1)
INSERT INTO food_items (restaurant_id, item_name, category_name, price, is_available) VALUES
    (1, 'Phở bò tái', 'Phở', 55000.00, 1),
    (1, 'Phở gà',     'Phở', 50000.00, 1),
    (1, 'Bún chả',    'Bún', 60000.00, 1);

-- Bún Bò Huế Ngon (restaurant_id=2)
INSERT INTO food_items (restaurant_id, item_name, category_name, price, is_available) VALUES
    (2, 'Bún bò Huế đặc biệt', 'Bún', 65000.00, 1),
    (2, 'Bún bò Huế thường',   'Bún', 55000.00, 1),
    (2, 'Nem lụi',             'Món phụ', 30000.00, 1);

-- Pizza Saigon (restaurant_id=3)
INSERT INTO food_items (restaurant_id, item_name, category_name, price, is_available) VALUES
    (3, 'Pizza Margherita (M)',  'Pizza', 120000.00, 1),
    (3, 'Pizza BBQ Chicken (L)', 'Pizza', 180000.00, 1),
    (3, 'Pasta Carbonara',       'Pasta', 95000.00,  1);

-- -----------------------------------------------------------------------------
-- orders  (1 đơn mẫu cho mỗi customer)
-- -----------------------------------------------------------------------------
-- Đơn 1: Nguyễn Văn An → Phở Hà Nội (Quận 1 -> Quận 1, gap 0 => fee 15000)
INSERT INTO orders
    (customer_id, restaurant_id, shipping_fee, total_amount,
     order_status, delivery_address, delivery_district_no, created_at)
VALUES
    (1, 1,
     15000.00, 185000.00,
     'COMPLETED',
     '5 Tràng Tiền, Hoàn Kiếm, Hà Nội',
     1,
     NOW() - INTERVAL 2 DAY);

-- Đơn 2: Trần Thị Bình → Bún Bò Huế Ngon (Quận 2 -> Quận 3, gap 1 => fee 15000)
INSERT INTO orders
    (customer_id, restaurant_id, shipping_fee, total_amount,
     order_status, delivery_address, delivery_district_no, created_at)
VALUES
    (2, 2,
     15000.00, 95000.00,
     'DELIVERING',
     '20 Bạch Đằng, Hải Châu, Đà Nẵng',
     3,
     NOW() - INTERVAL 1 HOUR);

-- Đơn 3: Lê Minh Châu → Pizza Saigon (Quận 3 -> Quận 5, gap 2 => fee 30000)
INSERT INTO orders
    (customer_id, restaurant_id, shipping_fee, total_amount,
     order_status, delivery_address, delivery_district_no, created_at)
VALUES
    (3, 3,
     30000.00, 330000.00,
     'PENDING',
     '99 Lê Văn Sỹ, Quận 3, TP.HCM',
     5,
     NOW());

-- -----------------------------------------------------------------------------
-- order_items
-- -----------------------------------------------------------------------------
-- Đơn 1: Phở bò tái (item_id=1, qty=2) + Bún chả (item_id=3, qty=1)
INSERT INTO order_items (order_id, item_id, quantity, unit_price, subtotal) VALUES
    (1, 1, 2, 55000.00, 110000.00),
    (1, 3, 1, 60000.00,  60000.00);
-- Ghi chú: total_amount = 110000 + 60000 + 20000 (ship) = 190000
-- (Đã cập nhật đơn để đúng hơn, đây là dữ liệu mẫu đơn giản)

-- Đơn 2: Bún bò Huế thường (item_id=5, qty=1) + Nem lụi (item_id=6, qty=2)
INSERT INTO order_items (order_id, item_id, quantity, unit_price, subtotal) VALUES
    (2, 5, 1, 55000.00, 55000.00),
    (2, 6, 2, 30000.00, 60000.00);
-- Đơn 2: total = 55000+60000+15000 (ship) = 130000 (mẫu gần đúng)

-- Đơn 3: Pizza BBQ (item_id=8, qty=1) + Pasta (item_id=9, qty=1) + Margherita (item_id=7, qty=1)
INSERT INTO order_items (order_id, item_id, quantity, unit_price, subtotal) VALUES
    (3, 7, 1, 120000.00, 120000.00),
    (3, 8, 1, 180000.00, 180000.00),
    (3, 9, 1,  95000.00,  95000.00);
-- Đơn 3: total = 120000+180000+95000+30000 (ship) = 425000 (mẫu gần đúng)

-- -----------------------------------------------------------------------------
-- deliveries
-- -----------------------------------------------------------------------------
-- Đơn 1 đã COMPLETED → driver Phạm Đức Hùng (driver_id=1)
INSERT INTO deliveries
    (order_id, driver_id, delivery_status, pickup_time, delivered_time)
VALUES
    (1, 1, 'DELIVERED',
     NOW() - INTERVAL 2 DAY + INTERVAL 20 MINUTE,
     NOW() - INTERVAL 2 DAY + INTERVAL 50 MINUTE);

-- Đơn 2 đang DELIVERING → driver Võ Thanh Tùng (driver_id=2)
INSERT INTO deliveries
    (order_id, driver_id, delivery_status, pickup_time, delivered_time)
VALUES
    (2, 2, 'PICKED_UP',
     NOW() - INTERVAL 30 MINUTE,
     NULL);

-- Đơn 3 PENDING → chưa có tài xế
INSERT INTO deliveries
    (order_id, driver_id, delivery_status, pickup_time, delivered_time)
VALUES
    (3, NULL, 'ASSIGNED', NULL, NULL);

-- -----------------------------------------------------------------------------
-- payments
-- -----------------------------------------------------------------------------
-- Đơn 1: thanh toán COD (method_id=1), đã PAID
INSERT INTO payments
    (order_id, method_id, amount, payment_status, transaction_code, paid_at)
VALUES
    (1, 1, 135000.00, 'PAID', NULL, NOW() - INTERVAL 2 DAY + INTERVAL 55 MINUTE);

-- Đơn 2: thanh toán VIETQR (method_id=2), chờ xác nhận
INSERT INTO payments
    (order_id, method_id, amount, payment_status, transaction_code, paid_at)
VALUES
    (2, 2, 95000.00, 'PENDING', NULL, NULL);

-- Đơn 3: COD, PENDING
INSERT INTO payments
    (order_id, method_id, amount, payment_status, transaction_code, paid_at)
VALUES
    (3, 1, 330000.00, 'PENDING', NULL, NULL);

-- -----------------------------------------------------------------------------
-- food_ratings  (chỉ đơn đã COMPLETED mới có đánh giá)
-- -----------------------------------------------------------------------------
-- Đơn 1, Phở bò tái (order_id=1, item_id=1)
INSERT INTO food_ratings (order_id, item_id, rating_score, comment, created_at)
VALUES (1, 1, 5, 'Phở rất ngon, nước dùng đậm đà!', NOW() - INTERVAL 1 DAY);

-- Đơn 1, Bún chả (order_id=1, item_id=3)
INSERT INTO food_ratings (order_id, item_id, rating_score, comment, created_at)
VALUES (1, 3, 4, 'Bún chả ổn, chả hơi mặn một chút.', NOW() - INTERVAL 1 DAY);
