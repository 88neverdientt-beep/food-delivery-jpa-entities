package vn.edu.ptit.fooddelivery.entity;

import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long deliveryId;

    /** has delivery: Orders 1 - 1 Deliveries (Deliveries tham gia toàn phần) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private CustomerOrder order;

    /** accepts delivery: Drivers 1 - N Deliveries; null cho đến khi có tài xế nhận đơn. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 30)
    @Builder.Default
    @ToString.Include
    private DeliveryStatus deliveryStatus = DeliveryStatus.ASSIGNED;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "delivered_time")
    private LocalDateTime deliveredTime;
}
