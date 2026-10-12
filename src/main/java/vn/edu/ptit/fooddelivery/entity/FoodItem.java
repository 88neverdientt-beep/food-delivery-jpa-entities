package vn.edu.ptit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long itemId;

    /** sells: Restaurants 1 - N Food_Items (mỗi món thuộc đúng một nhà hàng) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "item_name", nullable = false, length = 150)
    @ToString.Include
    private String itemName;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private boolean available = true;

    /** contains: phía Food_Items của quan hệ N:M qua Order_Items */
    @OneToMany(mappedBy = "foodItem")
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}
