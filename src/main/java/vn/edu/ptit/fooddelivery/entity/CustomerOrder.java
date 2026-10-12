package vn.edu.ptit.fooddelivery.entity;

import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể "Orders". Đặt tên class là CustomerOrder để tránh từ khóa ORDER trong JPQL/HQL;
 * bảng vẫn là "orders".
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long orderId;

    /** places: Customers 1 - N Orders */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** receives: Restaurants 1 - N Orders */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    /** Dẫn xuất: tổng subtotal các Order_Items + shipping_fee. */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    @Builder.Default
    @ToString.Include
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "delivery_district_no", nullable = false)
    @Builder.Default
    private Integer deliveryDistrictNo = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** contains: Orders 1 - N Order_Items (Order_Items là thực thể kết hợp của Orders N - M Food_Items) */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    /** paid by: Orders 1 - 1 Payments (FK nằm ở bảng payments) */
    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    /** has delivery: Orders 1 - 1 Deliveries (FK nằm ở bảng deliveries) */
    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Delivery delivery;

    /** Thêm một món vào đơn, chốt unit_price theo giá hiện tại của món. */
    public OrderItem addItem(FoodItem foodItem, int quantity) {
        OrderItem orderItem = OrderItem.builder()
                .id(new OrderItemId(null, foodItem.getItemId()))
                .order(this)
                .foodItem(foodItem)
                .quantity(quantity)
                .unitPrice(foodItem.getPrice())
                .build();
        orderItem.setSubtotal(orderItem.calculateSubtotal());
        orderItems.add(orderItem);
        return orderItem;
    }

    /** total_amount = tổng subtotal + shipping_fee. */
    public void recalculateTotalAmount() {
        BigDecimal itemsTotal = orderItems.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = itemsTotal.add(shippingFee == null ? BigDecimal.ZERO : shippingFee);
    }
}
