# PROMPT TRIỂN KHAI DỰ ÁN FOOD DELIVERY (DÀNH CHO AI AGENT)

## 1. Vai trò

Bạn là Kiến trúc sư Cơ sở dữ liệu kiêm Lập trình viên Backend Senior chuyên **Java** (Spring Boot, Spring Data JPA, Hibernate). Nhiệm vụ: triển khai backend cho hệ thống giao đồ ăn trực tuyến dựa **duy nhất** trên sơ đồ EER ở mục 3 và 4.

## 2. Quy tắc làm việc (bắt buộc)

1. Chỉ dùng các thực thể, thuộc tính, quan hệ được định nghĩa trong tài liệu này. Không tự thêm bảng, cột hay quan hệ mới. Nếu thấy thiếu, hãy dừng lại và hỏi.
2. Giữ đúng khóa chính, khóa ngoại và cardinality (1-1, 1-N, N-M).
3. Làm từng giai đoạn (mục 7). Xong giai đoạn nào thì tóm tắt ngắn những gì đã làm, chạy build/test, rồi mới sang giai đoạn tiếp theo.
4. Không sửa các file đã có (đặc biệt các entity ở mục 6) nếu chưa nêu rõ lý do và chưa được đồng ý.
5. Mọi thao tác ghi nhiều bảng phải nằm trong `@Transactional`.
6. Giải thích ngắn gọn, đi thẳng vào vấn đề. Không viết lại toàn bộ code khi chỉ sửa một phần.

## 3. Công nghệ

- Java 21, Spring Boot 3.x, Maven
- Spring Data JPA (Hibernate), MySQL 8.0, Lombok, Spring Boot DevTools
- Group `vn.edu.ptit`, artifact `food-delivery`, package gốc `vn.edu.ptit.fooddelivery`
- Kiến trúc phân tầng: `controller` → `service` → `repository` → `entity`; thêm `dto` và `exception`
- Quy ước: camelCase cho thuộc tính, PascalCase cho class, UPPER_SNAKE_CASE cho enum và hằng số. Tên bảng/cột trong DB dùng snake_case.

## 4. Mô hình dữ liệu (EER)

### 4.1 Thực thể và thuộc tính

| # | Thực thể | Thuộc tính |
|---|----------|-----------|
| 1 | Restaurants | `restaurant_id` (PK), `restaurant_name`, `email`, `password_hash`, `phone_number`, `address`, `latitude`, `longitude`, `is_active`, `created_at` |
| 2 | Food_Items | `item_id` (PK), `item_name`, `category_name`, `price`, `is_available` |
| 3 | Customers | `customer_id` (PK), `full_name`, `email`, `phone_number`, `password_hash`, `address`, `created_at` |
| 4 | Orders | `order_id` (PK), `distance_km` (dẫn xuất), `shipping_fee`, `total_amount` (dẫn xuất), `order_status`, `delivery_address`, `delivery_latitude`, `delivery_longitude`, `created_at` |
| 5 | Order_Items (thực thể kết hợp) | `order_id` (PK/FK), `item_id` (PK/FK), `quantity`, `unit_price`, `subtotal` (dẫn xuất) |
| 6 | Deliveries | `delivery_id` (PK), `delivery_status`, `pickup_time`, `delivered_time` |
| 7 | Drivers | `driver_id` (PK), `full_name`, `email`, `password_hash`, `phone_number`, `license_plate`, `is_online`, `is_busy`, `current_latitude`, `current_longitude`, `last_updated`, `created_at` |
| 8 | Payments | `payment_id` (PK), `amount`, `payment_status`, `transaction_code`, `paid_at` |
| 9 | Payment_Methods | `method_id` (PK), `method_code`, `method_name` |
| 10 | Food_Ratings | `rating_id` (PK), `rating_score`, `comment`, `created_at` |

### 4.2 Quan hệ và cardinality

| Quan hệ | Cardinality | Ghi chú |
|---------|-------------|---------|
| Restaurants **sells** Food_Items | 1 – N | Mỗi món thuộc đúng một nhà hàng; `Food_Items` tham gia toàn phần |
| Customers **places** Orders | 1 – N | `Orders` tham gia toàn phần |
| Restaurants **receives** Orders | 1 – N | `Orders` tham gia toàn phần |
| Orders **contains** Food_Items | N – M | Qua thực thể kết hợp `Order_Items` (khóa kép `order_id` + `item_id`) |
| Order_Items **has rating** Food_Ratings | 1 – 1 | Mỗi dòng món được đánh giá tối đa một lần |
| Orders **paid by** Payments | 1 – 1 | `Payments` tham gia toàn phần, FK `order_id` unique |
| Payments **uses** Payment_Methods | N – 1 | |
| Orders **has delivery** Deliveries | 1 – 1 | `Deliveries` tham gia toàn phần, FK `order_id` unique |
| Drivers **accepts delivery** Deliveries | 1 – N | `driver_id` cho phép null cho đến khi có tài xế nhận đơn |

### 4.3 Thuộc tính dẫn xuất (vẫn lưu cột để chốt giá trị lúc đặt)

- `Order_Items.subtotal` = `unit_price` × `quantity`
- `Orders.total_amount` = tổng `subtotal` các dòng món + `shipping_fee`
- `Orders.distance_km` = khoảng cách Haversine giữa tọa độ nhà hàng và `delivery_latitude/longitude`

### 4.4 Giá trị trạng thái (giả định tạm, có thể đổi)

- `OrderStatus`: PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, DELIVERING, COMPLETED, CANCELLED
- `DeliveryStatus`: ASSIGNED, PICKED_UP, DELIVERED, FAILED
- `PaymentStatus`: PENDING, PAID, FAILED, REFUNDED

## 5. Quy tắc nghiệp vụ

1. **Tạo đơn:** khách chọn món của **một** nhà hàng. `unit_price` lấy từ giá hiện tại của món, chỉ cho đặt món có `is_available = true` của nhà hàng `is_active = true`.
2. **Phí ship:** tính theo `distance_km` (Haversine). Công thức cụ thể (giá cơ bản, giá mỗi km) đặt trong file cấu hình `application.yml`, không hard-code.
3. **Tổng tiền:** `total_amount` = tiền món + `shipping_fee`. `Payments.amount` bằng `total_amount` tại thời điểm thanh toán.
4. **Thanh toán:** hai phương thức `COD` và `VIETQR` (bảng `Payment_Methods`). Với VietQR, trạng thái chuyển `PAID` khi nhận xác nhận từ webhook ngân hàng; với COD, chuyển `PAID` khi giao thành công. `transaction_code` có thể null (COD).
5. **Nhận đơn của tài xế:** chỉ tài xế `is_online = true` và `is_busy = false` mới nhận được đơn; ai bấm trước thì được (first-come, first-served). Dùng khóa bi quan (`@Lock(LockModeType.PESSIMISTIC_WRITE)` hoặc `SELECT ... FOR UPDATE`) để đảm bảo một đơn chỉ có đúng một tài xế. Khi nhận: tạo/cập nhật `Delivery`, đặt `is_busy = true`.
6. **Vòng đời đơn:** PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → DELIVERING → COMPLETED. Khách chỉ hủy được khi đơn còn PENDING; nhà hàng hủy được trước bước READY_FOR_PICKUP; sau khi tài xế đã nhận thì khách và nhà hàng không được hủy.
7. **Hoàn tiền:** hủy đơn đã thanh toán chuyển khoản thì chuyển `payment_status` thành `REFUNDED` ngay (cách đơn giản, chưa tích hợp cổng hoàn tiền).
8. **Đánh giá:** chỉ khách của đơn mới được đánh giá, và chỉ khi đơn đã `COMPLETED`. Mỗi `Order_Items` có tối đa một `Food_Ratings`.

> Vai trò Admin chưa có trong sơ đồ EER nên **chưa triển khai**. Nếu cần, hãy hỏi trước khi thêm.

## 6. Đầu vào đã có sẵn

- `food-delivery-jpa-entities.zip`: 10 entity, `OrderItemId` (khóa kép) và 3 enum, đặt trong `vn.edu.ptit.fooddelivery.entity`.
- Class của thực thể `Orders` tên là `CustomerOrder` (để tránh từ khóa `ORDER` của JPQL), bảng vẫn là `orders`.
- Giải nén vào `src/main/java` của project, **không viết lại** các class này.

## 7. Kế hoạch triển khai (làm tuần tự)

### Giai đoạn 1: Khởi tạo project
- Tạo project Spring Boot (Maven) với các dependency ở mục 3.
- Cấu hình `application.yml`: kết nối MySQL, `spring.jpa.hibernate.ddl-auto=validate`, `spring.jpa.open-in-view=false`, các tham số phí ship.
- Tiêu chí xong: ứng dụng khởi động được, kết nối DB thành công.

### Giai đoạn 2: Schema SQL
- Viết `sql/01_schema.sql` (MySQL 8.0) khớp tuyệt đối với entity: khóa chính, khóa ngoại, unique (`payments.order_id`, `deliveries.order_id`, `food_ratings(order_id, item_id)`, `transaction_code`, `email`), `CHECK` cho `quantity > 0`, `price >= 0`, `rating_score`.
- Viết `sql/02_dummy_data.sql` gồm dữ liệu mẫu cho các bảng, đặc biệt 2 dòng `Payment_Methods` (COD, VIETQR).
- Tiêu chí xong: chạy schema trên MySQL sạch không lỗi; ứng dụng khởi động với `ddl-auto=validate` không lỗi.

### Giai đoạn 3: Repository
- Mỗi entity một `JpaRepository`.
- Query cần có: món theo nhà hàng đang bán, đơn theo khách, đơn đang chờ tài xế (READY_FOR_PICKUP chưa có tài xế), tìm `Delivery` theo `order_id` có khóa `PESSIMISTIC_WRITE`.
- Tiêu chí xong: test `@DataJpaTest` cho các query tùy chỉnh.

### Giai đoạn 4: Service và DTO
- `OrderService.createOrder`: kiểm tra món, tính `distance_km`, `shipping_fee`, `subtotal`, `total_amount`; lưu `CustomerOrder` cùng `OrderItem` (dùng `CustomerOrder.addItem`).
- `PaymentService`: tạo thanh toán, xử lý webhook VietQR, hoàn tiền.
- `DeliveryService.acceptDelivery`: nhận đơn với khóa bi quan (quy tắc 5).
- `OrderLifecycleService`: chuyển trạng thái và hủy đơn theo quy tắc 6.
- `RatingService`: tạo đánh giá theo quy tắc 8.
- Dùng DTO (record) cho request/response, không trả entity ra ngoài. Dùng `@Valid` để kiểm tra đầu vào.
- Tiêu chí xong: unit test cho các service, bao gồm test hai tài xế nhận cùng một đơn thì chỉ một người thành công.

### Giai đoạn 5: REST API
- `POST /api/orders`, `GET /api/orders/{id}`, `PATCH /api/orders/{id}/cancel`
- `GET /api/restaurants/{id}/items`, `PATCH /api/orders/{id}/status` (nhà hàng)
- `POST /api/deliveries/{orderId}/accept`, `PATCH /api/deliveries/{id}/status` (tài xế)
- `POST /api/payments/webhook/vietqr`
- `POST /api/order-items/{orderId}/{itemId}/rating`
- Dùng `@RestControllerAdvice` để chuẩn hóa lỗi (404, 400, 409 khi đơn đã có tài xế).
- Tiêu chí xong: test tích hợp `@SpringBootTest` cho luồng chính: đặt đơn → quán xác nhận → tài xế nhận → giao → hoàn tất → đánh giá.

## 8. Định nghĩa hoàn thành

- `mvn clean verify` chạy thành công, tất cả test pass.
- Schema SQL và entity khớp nhau (`ddl-auto=validate` không báo lỗi).
- Không có thực thể, cột hay quan hệ nào ngoài mục 4.
- Mỗi giai đoạn có tóm tắt ngắn: file đã tạo/sửa, quyết định thiết kế, điểm còn mở.

## 9. Cách báo cáo cho tôi

Sau mỗi giai đoạn, trả lời theo mẫu:

```
Giai đoạn X: đã xong
- File đã tạo/sửa: ...
- Quyết định thiết kế: ...
- Cần tôi xác nhận: ...
- Bước tiếp theo: ...
```
