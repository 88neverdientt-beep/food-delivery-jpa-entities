-- =============================================================================
-- 01_schema.sql  –  Food Delivery  (MySQL 8.0)
-- Khớp tuyệt đối với JPA entities; chạy trên DB sạch, ddl-auto=validate.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS food_delivery
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE food_delivery;

-- -----------------------------------------------------------------------------
-- 1. payment_methods  (không phụ thuộc bảng nào)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_methods (
    method_id   BIGINT          NOT NULL AUTO_INCREMENT,
    method_code VARCHAR(30)     NOT NULL,
    method_name VARCHAR(100)    NOT NULL,
    CONSTRAINT pk_payment_methods PRIMARY KEY (method_id),
    CONSTRAINT uk_payment_methods_code UNIQUE (method_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. restaurants
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurants (
    restaurant_id   BIGINT          NOT NULL AUTO_INCREMENT,
    restaurant_name VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    phone_number    VARCHAR(20)     NOT NULL,
    address         VARCHAR(255)    NOT NULL,
    district_no     TINYINT         NOT NULL,
    latitude        DOUBLE          NULL,
    longitude       DOUBLE          NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME(6)     NOT NULL,
    CONSTRAINT pk_restaurants PRIMARY KEY (restaurant_id),
    CONSTRAINT uk_restaurants_email UNIQUE (email),
    CONSTRAINT chk_restaurants_district CHECK (district_no BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. customers
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id     BIGINT          NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(150)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    phone_number    VARCHAR(20)     NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    address         VARCHAR(255)    NULL,
    created_at      DATETIME(6)     NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (customer_id),
    CONSTRAINT uk_customers_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. drivers
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drivers (
    driver_id           BIGINT          NOT NULL AUTO_INCREMENT,
    full_name           VARCHAR(150)    NOT NULL,
    email               VARCHAR(150)    NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    phone_number        VARCHAR(20)     NOT NULL,
    license_plate       VARCHAR(20)     NOT NULL,
    is_online           TINYINT(1)      NOT NULL DEFAULT 0,
    is_busy             TINYINT(1)      NOT NULL DEFAULT 0,
    current_latitude    DOUBLE          NULL,
    current_longitude   DOUBLE          NULL,
    last_updated        DATETIME(6)     NOT NULL,
    created_at          DATETIME(6)     NOT NULL,
    CONSTRAINT pk_drivers PRIMARY KEY (driver_id),
    CONSTRAINT uk_drivers_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. food_items  (FK → restaurants)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_items (
    item_id         BIGINT          NOT NULL AUTO_INCREMENT,
    restaurant_id   BIGINT          NOT NULL,
    item_name       VARCHAR(150)    NOT NULL,
    category_name   VARCHAR(100)    NULL,
    price           DECIMAL(12,2)   NOT NULL,
    is_available    TINYINT(1)      NOT NULL DEFAULT 1,
    CONSTRAINT pk_food_items PRIMARY KEY (item_id),
    CONSTRAINT chk_food_items_price CHECK (price >= 0),
    CONSTRAINT fk_food_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. orders  (FK → customers, restaurants)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    order_id            BIGINT          NOT NULL AUTO_INCREMENT,
    customer_id         BIGINT          NOT NULL,
    restaurant_id       BIGINT          NOT NULL,
    shipping_fee        DECIMAL(12,2)   NOT NULL,
    total_amount        DECIMAL(12,2)   NOT NULL,
    order_status        VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    delivery_address    VARCHAR(255)    NOT NULL,
    delivery_district_no TINYINT        NOT NULL,
    created_at          DATETIME(6)     NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT chk_orders_status CHECK (order_status IN (
        'PENDING','CONFIRMED','PREPARING','READY_FOR_PICKUP',
        'DELIVERING','COMPLETED','CANCELLED'
    )),
    CONSTRAINT chk_orders_delivery_district CHECK (delivery_district_no BETWEEN 1 AND 10),
    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_orders_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. order_items  (FK → orders, food_items; PK kép)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    order_id    BIGINT          NOT NULL,
    item_id     BIGINT          NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  DECIMAL(12,2)   NOT NULL,
    subtotal    DECIMAL(12,2)   NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (order_id, item_id),
    CONSTRAINT chk_order_items_qty CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_order_items_food_item
        FOREIGN KEY (item_id) REFERENCES food_items(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. deliveries  (FK → orders, drivers; order_id UNIQUE → 1-1)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS deliveries (
    delivery_id     BIGINT      NOT NULL AUTO_INCREMENT,
    order_id        BIGINT      NOT NULL,
    driver_id       BIGINT      NULL,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED',
    pickup_time     DATETIME(6) NULL,
    delivered_time  DATETIME(6) NULL,
    CONSTRAINT pk_deliveries PRIMARY KEY (delivery_id),
    CONSTRAINT uk_deliveries_order UNIQUE (order_id),
    CONSTRAINT chk_deliveries_status CHECK (delivery_status IN (
        'ASSIGNED','PICKED_UP','DELIVERED','FAILED'
    )),
    CONSTRAINT fk_deliveries_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_deliveries_driver
        FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. payments  (FK → orders, payment_methods; order_id UNIQUE → 1-1)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id       BIGINT          NOT NULL AUTO_INCREMENT,
    order_id         BIGINT          NOT NULL,
    method_id        BIGINT          NOT NULL,
    amount           DECIMAL(12,2)   NOT NULL,
    payment_status   VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    transaction_code VARCHAR(100)    NULL,
    paid_at          DATETIME(6)     NULL,
    CONSTRAINT pk_payments PRIMARY KEY (payment_id),
    CONSTRAINT uk_payments_order UNIQUE (order_id),
    CONSTRAINT uk_payments_transaction_code UNIQUE (transaction_code),
    CONSTRAINT chk_payments_status CHECK (payment_status IN (
        'PENDING','PAID','FAILED','REFUNDED'
    )),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id),
    CONSTRAINT fk_payments_method
        FOREIGN KEY (method_id) REFERENCES payment_methods(method_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 10. food_ratings  (FK kép → order_items; UNIQUE(order_id, item_id) → 1-1)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_ratings (
    rating_id   BIGINT          NOT NULL AUTO_INCREMENT,
    order_id    BIGINT          NOT NULL,
    item_id     BIGINT          NOT NULL,
    rating_score INT            NOT NULL,
    comment     VARCHAR(1000)   NULL,
    created_at  DATETIME(6)     NOT NULL,
    CONSTRAINT pk_food_ratings PRIMARY KEY (rating_id),
    CONSTRAINT uk_food_ratings_order_item UNIQUE (order_id, item_id),
    CONSTRAINT chk_food_ratings_score CHECK (rating_score BETWEEN 1 AND 5),
    CONSTRAINT fk_food_ratings_order_item
        FOREIGN KEY (order_id, item_id) REFERENCES order_items(order_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
