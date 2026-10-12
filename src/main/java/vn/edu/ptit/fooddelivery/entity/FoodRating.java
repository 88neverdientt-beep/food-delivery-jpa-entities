package vn.edu.ptit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "food_ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_food_ratings_order_item",
                columnNames = {"order_id", "item_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class FoodRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long ratingId;

    /** has rating: Order_Items 1 - 1 Food_Ratings (khóa ngoại kép + unique) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false),
            @JoinColumn(name = "item_id", referencedColumnName = "item_id", nullable = false)
    })
    private OrderItem orderItem;

    @Column(name = "rating_score", nullable = false)
    @ToString.Include
    private Integer ratingScore;

    @Column(name = "comment", length = 1000)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
