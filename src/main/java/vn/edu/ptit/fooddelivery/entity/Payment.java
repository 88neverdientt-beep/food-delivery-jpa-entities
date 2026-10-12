package vn.edu.ptit.fooddelivery.entity;

import vn.edu.ptit.fooddelivery.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long paymentId;

    /** paid by: Orders 1 - 1 Payments */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private CustomerOrder order;

    /** uses: Payments N - 1 Payment_Methods */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    @Builder.Default
    @ToString.Include
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    /** Null với phương thức không có mã giao dịch (ví dụ tiền mặt). */
    @Column(name = "transaction_code", unique = true, length = 100)
    private String transactionCode;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
