package vn.edu.ptit.fooddelivery.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import vn.edu.ptit.fooddelivery.entity.Delivery;

import jakarta.persistence.QueryHint;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /**
     * Tìm delivery theo order_id (không khóa).
     * Dùng khi đọc thông tin thông thường.
     */
    Optional<Delivery> findByOrderOrderId(Long orderId);

    /**
     * Tìm delivery theo order_id với khóa bi quan PESSIMISTIC_WRITE (SELECT … FOR UPDATE).
     * Đảm bảo chỉ một tài xế nhận được đơn (quy tắc nghiệp vụ 5.5).
     * Phải gọi trong phạm vi @Transactional.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT d FROM Delivery d WHERE d.order.orderId = :orderId")
    Optional<Delivery> findByOrderIdWithLock(@Param("orderId") Long orderId);
}
