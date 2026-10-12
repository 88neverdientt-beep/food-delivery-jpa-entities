package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.fooddelivery.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Tìm thanh toán theo đơn hàng. */
    Optional<Payment> findByOrderOrderId(Long orderId);
}
