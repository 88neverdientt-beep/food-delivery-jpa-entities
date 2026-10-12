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

-- 2. Liệt kê các đơn hàng cũ (sẽ giữ nguyên NULL theo Phương án B - bảo toàn dữ liệu lịch sử):
SELECT order_id, customer_id, restaurant_id, delivery_address, total_amount
FROM orders
WHERE delivery_district_no IS NULL;

-- (Phương án B: KHÔNG gán mặc định Quận 1 cho đơn cũ để không làm sai lệch báo cáo/lịch sử)

-- 3. Áp đặt ràng buộc:
-- - restaurants: Bắt buộc NOT NULL và BETWEEN 1 AND 10
ALTER TABLE restaurants
    MODIFY COLUMN district_no TINYINT NOT NULL,
    ADD CONSTRAINT chk_restaurants_district CHECK (district_no BETWEEN 1 AND 10);

-- - orders: Cho phép NULL cho đơn cũ; nếu có giá trị (đơn mới) thì phải BETWEEN 1 AND 10
ALTER TABLE orders
    MODIFY COLUMN delivery_district_no TINYINT NULL,
    ADD CONSTRAINT chk_orders_delivery_district CHECK (delivery_district_no IS NULL OR (delivery_district_no BETWEEN 1 AND 10));


-- -----------------------------------------------------------------------------
-- BƯỚC (c): Xóa cột cũ → ĐÃ TÁCH SANG FILE RIÊNG
-- Xem: sql/06_drop_legacy_gps_columns.sql
-- Chạy file đó SAU CÙNG, sau khi đã rà soát dữ liệu và deploy thành công.
-- -----------------------------------------------------------------------------
