package vn.edu.ptit.fooddelivery.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "method_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long methodId;

    @Column(name = "method_code", nullable = false, unique = true, length = 30)
    @ToString.Include
    private String methodCode;

    @Column(name = "method_name", nullable = false, length = 100)
    private String methodName;

    /** uses: Payment_Methods 1 - N Payments */
    @OneToMany(mappedBy = "paymentMethod")
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}
