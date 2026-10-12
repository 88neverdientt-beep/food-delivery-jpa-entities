-- =============================================================================
-- Migration Script: 04_cleanup_old_columns.sql
-- Mục đích: Xóa các cột cũ không còn sử dụng trong bảng orders
-- LƯU Ý: CHỈ CHẠY SCRIPT NÀY SAU CÙNG khi hệ thống mới đã hoạt động hoàn toàn ổn định.
-- =============================================================================

ALTER TABLE orders
    DROP COLUMN distance_km,
    DROP COLUMN delivery_latitude,
    DROP COLUMN delivery_longitude;
