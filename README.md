# Food Delivery

Hệ thống giao đồ ăn trực tuyến full-stack, mô phỏng luồng vận hành giữa **khách hàng**, **nhà hàng** và **tài xế**. Backend xây bằng Spring Boot 3 và JPA trên MySQL; frontend là ứng dụng React (Vite + Tailwind CSS).

> Học viện Công nghệ Bưu chính Viễn thông (PTIT) — `vn.edu.ptit.fooddelivery`

> **Lưu ý: đây là bản demo học tập.** Xác thực hiện không dùng JWT (phiên lưu phía client) và mật khẩu đang lưu dạng thô. Vui lòng **không dùng email hoặc mật khẩu thật** khi thử bản demo công khai. Xem mục [Ghi chú bảo mật](#ghi-chú-bảo-mật-demo).

## Demo

- Giao diện: [(https://food-delivery-jpa-entities.vercel.app/)](https://food-delivery-jpa-entities.vercel.app/)
- Backend và MySQL: `(http://localhost:8080/api)`

## Tổng quan

Khách duyệt nhà hàng theo quận, đặt món, xem trước phí ship, thanh toán và theo dõi đơn. Nhà hàng xác nhận và chuẩn bị món. Tài xế tự chọn đơn (first-come, first-served) và cập nhật hành trình giao hàng. Quy tắc nghiệp vụ được kiểm soát ở tầng service; schema MySQL khớp với JPA entity (`ddl-auto: validate`).

## Tính năng chính

### Khách hàng
- Đăng ký, đăng nhập
- Xem danh sách nhà hàng đang hoạt động, lọc theo quận, phân trang
- Xem thực đơn, thêm món vào giỏ
- Xem trước phí ship và tổng tiền trước khi đặt (`POST /api/orders/preview`)
- Đặt hàng, theo dõi trạng thái, xem lịch sử đơn
- Hủy đơn khi còn `PENDING`
- Đánh giá từng món (1–5 sao) sau khi đơn `COMPLETED` (mỗi món một lần)

### Nhà hàng
- Đăng ký, đăng nhập, gắn quận hoạt động (1–10)
- Quản lý đơn: chuyển trạng thái theo sơ đồ một chiều
- Hủy đơn trước khi sẵn sàng giao (`PENDING` / `CONFIRMED` / `PREPARING`)

### Tài xế
- Đăng ký, đăng nhập, bật/tắt online, cập nhật vị trí GPS
- Danh sách đơn chờ nhận (`READY_FOR_PICKUP`, chưa có tài xế)
- Nhận đơn first-come, first-served với khóa bi quan (`SELECT ... FOR UPDATE`)
- Cập nhật hành trình: `ASSIGNED` → `PICKED_UP` → `DELIVERED` / `FAILED`

### Thanh toán
- **COD**: thanh toán khi nhận hàng
- **VietQR**: tạo payment `PENDING`, xác nhận qua webhook ngân hàng
- Hủy đơn đã thanh toán VietQR thì tự chuyển `REFUNDED`

## Vòng đời đơn hàng

```
PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → DELIVERING → COMPLETED
   ↘            ↘           ↘
                      CANCELLED
```

| Bước | Ai thực hiện | Ghi chú |
|------|--------------|---------|
| `PENDING` → `CONFIRMED` → `PREPARING` → `READY_FOR_PICKUP` | Nhà hàng | Khi `READY_FOR_PICKUP`, hệ thống tạo bản ghi `Delivery` (chưa gán tài xế) |
| `READY_FOR_PICKUP` → `DELIVERING` | Tài xế (lấy hàng) | Đồng bộ trạng thái đơn |
| `DELIVERING` → `COMPLETED` | Tài xế (đã giao) | Giải phóng `is_busy` của tài xế |
| Hủy bởi khách | Khách | Chỉ khi `PENDING` |
| Hủy bởi nhà hàng | Nhà hàng | Trước `READY_FOR_PICKUP` |
| `FAILED` | Tài xế | Đơn `CANCELLED`, tài xế được giải phóng |

## Phí giao hàng theo quận

Thành phố được mô hình hóa thành **10 quận**. Phí ship tính theo khoảng cách giữa các quận:

```
gap = |quận_nhà_hàng − quận_giao|

gap = 0   →  15.000 VND   (cùng quận)
gap >= 1  →  gap × 15.000 VND
```

Tham số cấu hình trong `application.yml` (`app.shipping`): `total-districts`, `same-district-fee`, `per-gap-fee`.

## Công nghệ

| Tầng | Công nghệ |
|------|-----------|
| Backend | Java 21, Spring Boot 3.3.4, Spring Web, Spring Data JPA, Bean Validation |
| ORM / DB | Hibernate, MySQL 8 (`utf8mb4`), schema-first SQL |
| Frontend | React 18, Vite 6, React Router 6, Tailwind CSS, Axios, Lucide |
| Test | JUnit, Spring Boot Test, Mockito, H2 in-memory (`MODE=MySQL`) |
| Khác | Lombok, Maven |

## Kiến trúc

```
┌──────────────────────────────────────────────┐
│               React SPA (Vite)               │
│  Customer / Restaurant / Driver dashboards   │
└───────────────────────┬──────────────────────┘
                        │ REST /api/**
┌───────────────────────▼──────────────────────┐
│                 Spring Boot                  │
│ Controller → Service → Repository (JPA)      │
│ GlobalExceptionHandler (404/400/409/500)     │
└───────────────────────┬──────────────────────┘
                        │
                 MySQL 8 · food_delivery
```

Khi build production, frontend được đóng gói vào `src/main/resources/static`. `WebConfig` phục vụ SPA: mọi route phía client chuyển về `index.html`, riêng `/api/**` đi vào REST controller.

### Mô hình dữ liệu (10 bảng)

`payment_methods` · `restaurants` · `customers` · `drivers` · `food_items` · `orders` · `order_items` (khóa chính kép) · `deliveries` (1–1 với order) · `payments` (1–1 với order) · `food_ratings` (1–1 theo từng món trong đơn)

## REST API (tóm tắt)

| Nhóm | Endpoint | Mô tả |
|------|----------|-------|
| Auth | `POST /api/auth/{customer\|restaurant\|driver}/register\|login` | Đăng ký / đăng nhập theo vai trò |
| District | `GET /api/districts` | Danh sách quận từ cấu hình |
| Restaurant | `GET /api/restaurants` | Nhà hàng đang hoạt động, lọc quận, phân trang |
| | `GET /api/restaurants/{id}/items` | Thực đơn đang bán |
| | `GET /api/restaurants/{id}/orders` | Đơn của nhà hàng |
| Order | `POST /api/orders/preview` | Xem trước phí ship và tổng tiền |
| | `POST /api/orders` | Đặt đơn |
| | `GET /api/orders/{id}` | Chi tiết đơn |
| | `PATCH /api/orders/{id}/status` | Nhà hàng chuyển trạng thái |
| | `PATCH /api/orders/{id}/cancel` | Hủy đơn (khách / nhà hàng) |
| Customer | `GET /api/customers/{id}/orders` | Lịch sử đơn |
| Payment | `GET /api/payment-methods` | COD, VietQR |
| | `POST /api/payments` | Tạo thanh toán |
| | `POST /api/payments/webhook/vietqr` | Webhook xác nhận VietQR |
| Delivery | `POST /api/deliveries/{orderId}/accept` | Tài xế nhận đơn (khóa bi quan) |
| | `PATCH /api/deliveries/{id}/status` | Cập nhật hành trình |
| Driver | `PATCH /api/drivers/{id}/online` | Online / offline |
| | `PATCH /api/drivers/{id}/location` | Cập nhật GPS |
| | `GET /api/drivers/deliveries/available` | Đơn chờ nhận |
| | `GET /api/drivers/{id}/active-delivery` | Đơn đang giao |
| Rating | `POST /api/order-items/{orderId}/{itemId}/rating` | Đánh giá món |

## Cấu trúc thư mục

```
├── pom.xml
├── sql/
│   ├── 01_schema.sql            # Schema MySQL khớp JPA
│   ├── 02_dummy_data.sql        # Dữ liệu mẫu
│   └── 03–06_*.sql              # Migration / dọn cột cũ
├── src/main/java/.../fooddelivery/
│   ├── controller/              # REST API
│   ├── service/                 # Nghiệp vụ và vòng đời đơn
│   ├── repository/
│   ├── entity/ + enums/
│   ├── dto/request|response/
│   ├── exception/
│   ├── config/                  # CORS/SPA, shipping
│   └── util/ShippingFeeCalculator
├── src/test/                    # Unit + integration (H2)
├── frontend/                    # React + Vite
└── run.bat                      # Chạy JAR đã đóng gói
```

## Yêu cầu

- JDK 21+
- Maven 3.9+
- MySQL 8.0+
- Node.js 18+ (khi phát triển frontend riêng)

## Cài đặt và chạy

### 1. Cơ sở dữ liệu

```sql
-- trong MySQL
source sql/01_schema.sql
source sql/02_dummy_data.sql
```

Cấu hình kết nối trong `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_delivery?...
    username: root
    password: root
server:
  port: 8080
```

### 2. Backend

```bash
mvn spring-boot:run
```

API: `http://localhost:8080/api`

### 3. Frontend (chế độ dev)

```bash
cd frontend
npm install
npm run dev
```

Giao diện: `http://localhost:5173`

### 4. Chạy production (JAR gồm cả SPA)

Build frontend vào `src/main/resources/static`, rồi:

```bash
mvn clean package
java -jar target/food-delivery-0.0.1-SNAPSHOT.jar
```

Hoặc dùng `run.bat` trên Windows.

### 5. Kiểm thử

```bash
mvn test
```

Test chạy trên H2 in-memory, không cần MySQL. Gồm unit test service/util và integration test luồng đặt hàng và phí ship theo quận.

## Triển khai

Bản demo giao diện được triển khai trên Vercel. Vercel chỉ phục vụ phần frontend, nên backend Spring Boot và MySQL cần chạy ở một nơi khác và frontend phải gọi tới địa chỉ backend đó.

<!-- Điền: backend chạy ở đâu, biến môi trường/ file cấu hình địa chỉ API của frontend -->

## Ghi chú bảo mật (demo)

- Xác thực hiện không dùng JWT; phiên được lưu phía client và các API nhận id người dùng trực tiếp trên đường dẫn.
- Mật khẩu đang lưu dạng thô để phục vụ demo. Cần thay bằng BCrypt (và Spring Security) trước khi dùng thật.
- Nhận đơn dùng khóa bi quan để tránh hai tài xế nhận cùng một đơn.
- Hibernate `ddl-auto: validate`: schema phải được tạo bằng SQL, không để JPA tự sinh bảng.

## Giấy phép

Dự án phục vụ mục đích học tập / đồ án. Hãy thêm file `LICENSE` (MIT, Apache-2.0, ...) khi công khai repo.
