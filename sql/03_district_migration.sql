-- =============================================================================
-- Migration Script: 03_district_migration.sql
-- Mục đích: Thêm phạm vi 1 thành phố 10 quận và phí ship theo quận cho hệ thống Food Delivery
-- =============================================================================
-- QUAN TRỌNG:
-- 1. VUI LÒNG SAO LƯU (BACKUP) TOÀN BỘ CƠ SỞ DỮ LIỆU TRƯỚC KHI CHẠY SCRIPT NÀY.
-- 2. KHÔNG tự động chạy script lên DB production/thực tế mà hãy chạy thử nghiệm
--    trên DB test/trống trước.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- BƯỚC (a): Thêm cột mới (cho phép NULL ban đầu) và chỉnh sửa cột tọa độ
-- -----------------------------------------------------------------------------

-- 1. Thêm cột district_no vào bảng restaurants (cho phép null tạm thời)
--    Đồng thời chuyển latitude và longitude thành cho phép null.
ALTER TABLE restaurants
    ADD COLUMN district_no TINYINT NULL AFTER address,
    MODIFY COLUMN latitude DOUBLE NULL,
    MODIFY COLUMN longitude DOUBLE NULL;

-- 2. Thêm cột delivery_district_no vào bảng orders (cho phép null tạm thời)
ALTER TABLE orders
    ADD COLUMN delivery_district_no TINYINT NULL AFTER delivery_address;


-- -----------------------------------------------------------------------------
-- BƯỚC (b): Gán giá trị mặc định (Quận 1) cho các bản ghi cũ và áp đặt ràng buộc
-- -----------------------------------------------------------------------------

-- 1. Liệt kê các nhà hàng cũ sẽ bị gán quận mặc định (Quận 1):
SELECT restaurant_id, restaurant_name, email, address
FROM restaurants
WHERE district_no IS NULL;

-- Gán mặc định Quận 1 cho các nhà hàng cũ:
UPDATE restaurants
SET district_no = 1
WHERE district_no IS NULL;

-- 2. Liệt kê các đơn hàng cũ sẽ bị gán quận giao mặc định (Quận 1):
SELECT order_id, customer_id, restaurant_id, delivery_address, total_amount
FROM orders
WHERE delivery_district_no IS NULL;

-- Gán mặc định Quận 1 cho các đơn hàng cũ:
UPDATE orders
SET delivery_district_no = 1
WHERE delivery_district_no IS NULL;

-- 3. Áp đặt NOT NULL và ràng buộc CHECK (district_no từ 1 đến 10) sau khi dữ liệu đã sạch:
ALTER TABLE restaurants
    MODIFY COLUMN district_no TINYINT NOT NULL,
    ADD CONSTRAINT chk_restaurants_district CHECK (district_no BETWEEN 1 AND 10);

ALTER TABLE orders
    MODIFY COLUMN delivery_district_no TINYINT NOT NULL,
    ADD CONSTRAINT chk_orders_delivery_district CHECK (delivery_district_no BETWEEN 1 AND 10);


-- -----------------------------------------------------------------------------
-- BƯỚC (c): Xóa các cột cũ không còn sử dụng
-- LƯU Ý: Bước này CHỈ CHẠY SAU CÙNG khi mã nguồn backend & frontend mới đã được deploy
--        và hoạt động ổn định hoàn toàn!
-- -----------------------------------------------------------------------------
-- ALTER TABLE orders
--     DROP COLUMN distance_km,
--     DROP COLUMN delivery_latitude,
--     DROP COLUMN delivery_longitude;
