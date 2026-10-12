package vn.edu.ptit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long restaurantId;

    @Column(name = "restaurant_name", nullable = false, length = 150)
    @ToString.Include
    private String restaurantName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** sells: Restaurants 1 - N Food_Items */
    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<FoodItem> foodItems = new ArrayList<>();

    /** receives: Restaurants 1 - N Orders */
    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<CustomerOrder> orders = new ArrayList<>();
}
