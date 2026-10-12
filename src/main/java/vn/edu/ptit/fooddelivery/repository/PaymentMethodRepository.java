package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.fooddelivery.entity.PaymentMethod;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    /** Tìm phương thức thanh toán theo code (VD: "COD", "VIETQR"). */
    Optional<PaymentMethod> findByMethodCode(String methodCode);
}
