package vn.edu.ptit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Thực thể kết hợp của quan hệ contains (Orders N - M Food_Items). */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class OrderItem {

    @EmbeddedId
    @EqualsAndHashCode.Include
    @ToString.Include
    private OrderItemId id;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @MapsId("itemId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private FoodItem foodItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** Giá món tại thời điểm đặt. */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** Dẫn xuất: unit_price x quantity. */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /** has rating: Order_Items 1 - 1 Food_Ratings (FK nằm ở bảng food_ratings) */
    @OneToOne(mappedBy = "orderItem", fetch = FetchType.LAZY)
    private FoodRating rating;

    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @PrePersist
    @PreUpdate
    void syncSubtotal() {
        this.subtotal = calculateSubtotal();
    }
}
