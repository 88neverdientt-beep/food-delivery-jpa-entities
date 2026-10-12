-- =============================================================================
-- Drop Columns Script: 06_drop_legacy_gps_columns.sql
-- Mục đích: Xóa các cột GPS cũ không còn dùng sau khi đã chuyển sang district_no.
--
-- ⚠️  CHỈ CHẠY KHI ĐÃ THỎA MÃN TẤT CẢ ĐIỀU KIỆN SAU:
--     1. Đã SAO LƯU (BACKUP) toàn bộ DB.
--     2. Đã chạy xong 04_district_audit.sql và rà soát dữ liệu.
--     3. Đã sửa quận đúng cho từng nhà hàng bằng 05_district_fix_template.sql.
--     4. Đã chạy thử nghiệm toàn bộ trên DB test/trống và KHÔNG có lỗi.
--     5. Phiên bản backend và frontend mới đã được deploy và hoạt động ổn định.
--
-- SAU KHI XÓA, CÁC CỘT NÀY KHÔNG THỂ KHÔI PHỤC (chỉ còn trong backup).
-- =============================================================================

ALTER TABLE orders
    DROP COLUMN distance_km,
    DROP COLUMN delivery_latitude,
    DROP COLUMN delivery_longitude;

-- Kiểm tra kết quả:
DESCRIBE orders;
