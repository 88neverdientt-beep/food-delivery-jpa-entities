package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    /**
     * Lấy toàn bộ đơn hàng của một khách, mới nhất trước.
     */
    List<CustomerOrder> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Lấy các đơn đang chờ tài xế nhận:
     * - Trạng thái READY_FOR_PICKUP
     * - Chưa có delivery record HOẶC delivery đã tồn tại nhưng driver còn null
     * Dùng cho màn hình tài xế chọn đơn (quy tắc nghiệp vụ 5.5).
     */
    @Query("""
            SELECT o FROM CustomerOrder o
            LEFT JOIN o.delivery d
            WHERE o.orderStatus = :status
              AND (d IS NULL OR d.driver IS NULL)
            ORDER BY o.createdAt ASC
            """)
    List<CustomerOrder> findAwaitingDriver(@Param("status") OrderStatus status);

    /**
     * Shortcut: gọi findAwaitingDriver với trạng thái READY_FOR_PICKUP cố định.
     */
    default List<CustomerOrder> findAwaitingDriver() {
        return findAwaitingDriver(OrderStatus.READY_FOR_PICKUP);
    }
}
