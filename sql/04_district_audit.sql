-- =============================================================================
-- Audit Script: 04_district_audit.sql
-- Mục đích: Rà soát dữ liệu CŨ trước khi chạy migration. CHỈ ĐỌC, không sửa gì.
-- Chạy trên DB thật để xác nhận trước khi sửa quận.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- PHẦN 1: Danh sách toàn bộ nhà hàng và quận hiện tại
-- ---------------------------------------------------------------------------
-- Sau khi chạy 03_district_migration.sql bước (b), cột district_no đã tồn tại.
-- Câu này liệt kê đầy đủ để bạn xác nhận quận có đúng không.
SELECT
    r.restaurant_id,
    r.restaurant_name,
    r.address,
    r.district_no  AS district_no_hien_tai,
    CASE WHEN r.district_no = 1 THEN '⚠ Có thể là giá trị mặc định' ELSE 'OK' END AS ghi_chu
FROM restaurants r
ORDER BY r.restaurant_id;


-- ---------------------------------------------------------------------------
-- PHẦN 2: Tổng số đơn hàng cũ bị gán quận mặc định
-- (Nếu chạy TRƯỚC bước (b) của 03_district_migration → đếm dòng IS NULL)
-- (Nếu chạy SAU bước (b)       → không thể phân biệt được đơn cũ/mới → xem phần 3)
-- ---------------------------------------------------------------------------
-- Chạy TRƯỚC bước (b):
SELECT COUNT(*) AS so_don_cu_chua_co_quan
FROM orders
WHERE delivery_district_no IS NULL;

-- ---------------------------------------------------------------------------
-- PHẦN 3: 20 đơn hàng gần nhất có quận giao = 1 (có thể là mặc định)
-- Dùng để rà soát sau khi bước (b) đã chạy.
-- Chú ý: đơn mới đặt ở Quận 1 thật sự cũng sẽ xuất hiện ở đây.
-- ---------------------------------------------------------------------------
SELECT
    o.order_id,
    o.customer_id,
    o.restaurant_id,
    r.restaurant_name,
    r.district_no           AS quan_nha_hang,
    o.delivery_district_no  AS quan_giao,
    o.delivery_address,
    o.shipping_fee,
    o.total_amount,
    o.order_status,
    o.created_at
FROM orders o
LEFT JOIN restaurants r ON r.restaurant_id = o.restaurant_id
WHERE o.delivery_district_no = 1
ORDER BY o.created_at DESC
LIMIT 20;


-- ---------------------------------------------------------------------------
-- PHẦN 4: Thống kê phân bố đơn hàng theo quận giao (toàn bộ)
-- ---------------------------------------------------------------------------
SELECT
    delivery_district_no AS quan_giao,
    COUNT(*)             AS so_don,
    MIN(created_at)      AS don_cu_nhat,
    MAX(created_at)      AS don_moi_nhat
FROM orders
GROUP BY delivery_district_no
ORDER BY delivery_district_no;


-- ---------------------------------------------------------------------------
-- PHẦN 5: Thống kê nhà hàng theo quận
-- ---------------------------------------------------------------------------
SELECT
    district_no AS quan,
    COUNT(*)    AS so_nha_hang
FROM restaurants
GROUP BY district_no
ORDER BY district_no;
