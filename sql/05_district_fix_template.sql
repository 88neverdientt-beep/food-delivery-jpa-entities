-- =============================================================================
-- Fix Template: 05_district_fix_template.sql
-- Mục đích: Mẫu câu lệnh để bạn tự điền quận ĐÚNG cho từng nhà hàng.
-- Cách dùng:
--   1. Chạy 04_district_audit.sql để xem danh sách nhà hàng.
--   2. Xác định quận thật sự của mỗi nhà hàng.
--   3. Điền restaurant_id và district_no vào dấu ? bên dưới.
--   4. Bọc trong transaction, kiểm tra kết quả rồi COMMIT.
-- =============================================================================

-- CHẠY TRONG TRANSACTION ĐỂ AN TOÀN:
START TRANSACTION;

-- -------------------------------------------------------------------
-- Sửa quận cho từng nhà hàng:
-- Thay ? đầu tiên = số quận đúng (1-10)
-- Thay ? thứ hai = restaurant_id của nhà hàng đó
-- Sao chép thêm dòng nếu có nhiều nhà hàng cần sửa.
-- -------------------------------------------------------------------
UPDATE restaurants SET district_no = ? WHERE restaurant_id = ?;
UPDATE restaurants SET district_no = ? WHERE restaurant_id = ?;
UPDATE restaurants SET district_no = ? WHERE restaurant_id = ?;
-- ... thêm dòng tương tự cho các nhà hàng còn lại ...


-- -------------------------------------------------------------------
-- Sau khi sửa xong nhà hàng, cập nhật lại phí ship cho đơn hàng cũ
-- (KHÔNG BẮT BUỘC nếu bạn chấp nhận giữ nguyên phí ship đã ghi nhận):
--
-- Nếu muốn tính lại phí ship theo quận đúng:
--   gap = ABS(r.district_no - o.delivery_district_no)
--   fee = IF(gap = 0, 15000, gap * 15000)
--
-- UPDATE orders o
-- JOIN restaurants r ON r.restaurant_id = o.restaurant_id
-- SET
--   o.shipping_fee  = IF(ABS(r.district_no - o.delivery_district_no) = 0, 15000,
--                         ABS(r.district_no - o.delivery_district_no) * 15000),
--   o.total_amount  = o.total_amount
--                     - <phí_ship_cũ>      -- tính thủ công hoặc dùng subquery
--                     + IF(ABS(r.district_no - o.delivery_district_no) = 0, 15000,
--                           ABS(r.district_no - o.delivery_district_no) * 15000)
-- WHERE o.order_id IN (...); -- giới hạn các đơn cũ cần sửa
-- -------------------------------------------------------------------

-- Kiểm tra kết quả trước khi commit:
SELECT restaurant_id, restaurant_name, address, district_no FROM restaurants ORDER BY restaurant_id;

-- Nếu kết quả đúng → COMMIT, nếu sai → ROLLBACK
-- COMMIT;
-- ROLLBACK;
