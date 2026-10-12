package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.fooddelivery.entity.OrderItem;
import vn.edu.ptit.fooddelivery.entity.OrderItemId;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
