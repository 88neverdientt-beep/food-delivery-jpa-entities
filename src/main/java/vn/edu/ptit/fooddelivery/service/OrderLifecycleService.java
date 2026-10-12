package vn.edu.ptit.fooddelivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.Delivery;
import vn.edu.ptit.fooddelivery.entity.Payment;
import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.entity.enums.PaymentStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.DeliveryRepository;
import vn.edu.ptit.fooddelivery.repository.PaymentRepository;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Quản lý vòng đời đơn hàng theo sơ đồ trạng thái (quy tắc nghiệp vụ 5.6):
 * PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → DELIVERING → COMPLETED
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderLifecycleService {

    /** Bảng chuyển trạng thái hợp lệ (một chiều). */
    private static final Map<OrderStatus, OrderStatus> NEXT_STATUS = Map.of(
            OrderStatus.PENDING,          OrderStatus.CONFIRMED,
            OrderStatus.CONFIRMED,        OrderStatus.PREPARING,
            OrderStatus.PREPARING,        OrderStatus.READY_FOR_PICKUP,
            OrderStatus.READY_FOR_PICKUP, OrderStatus.DELIVERING,
            OrderStatus.DELIVERING,       OrderStatus.COMPLETED
    );

    /** Trạng thái nhà hàng được phép hủy (trước khi tài xế nhận). */
    private static final Set<OrderStatus> RESTAURANT_CANCELLABLE = EnumSet.of(
            OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING
    );

    private final CustomerOrderRepository orderRepository;
    private final DeliveryRepository      deliveryRepository;
    private final PaymentRepository       paymentRepository;

    // ─── Commands ─────────────────────────────────────────────────────────────

    /**
     * Chuyển đơn sang trạng thái tiếp theo hợp lệ.
     * Khi chuyển sang READY_FOR_PICKUP: tạo Delivery record (driver=null).
     */
    @Transactional
    public OrderResponse advanceStatus(Long orderId, OrderStatus targetStatus) {
        CustomerOrder order = findOrThrow(orderId);

        OrderStatus expected = NEXT_STATUS.get(order.getOrderStatus());
        if (expected == null || expected != targetStatus) {
            throw new BusinessException(
                    "Cannot advance from " + order.getOrderStatus() + " to " + targetStatus);
        }

        order.setOrderStatus(targetStatus);

        // Khi nhà hàng đánh dấu READY_FOR_PICKUP → tạo Delivery (chờ tài xế nhận)
        if (targetStatus == OrderStatus.READY_FOR_PICKUP) {
            boolean deliveryExists = deliveryRepository.findByOrderOrderId(orderId).isPresent();
            if (!deliveryExists) {
                Delivery delivery = Delivery.builder()
                        .order(order)
                        .deliveryStatus(DeliveryStatus.ASSIGNED)
                        .build();
                deliveryRepository.save(delivery);
            }
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    /**
     * Khách hủy đơn – chỉ được khi PENDING. (Quy tắc 5.6)
     */
    @Transactional
    public OrderResponse cancelByCustomer(Long orderId, Long customerId) {
        CustomerOrder order = findOrThrow(orderId);

        if (!order.getCustomer().getCustomerId().equals(customerId)) {
            throw new BusinessException("Order does not belong to customer: " + customerId);
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Customer can only cancel PENDING orders. Current status: " + order.getOrderStatus());
        }

        return cancelAndRefundIfNeeded(order);
    }

    /**
     * Nhà hàng hủy đơn – được phép trước READY_FOR_PICKUP. (Quy tắc 5.6)
     */
    @Transactional
    public OrderResponse cancelByRestaurant(Long orderId, Long restaurantId) {
        CustomerOrder order = findOrThrow(orderId);

        if (!order.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new BusinessException("Order does not belong to restaurant: " + restaurantId);
        }
        if (!RESTAURANT_CANCELLABLE.contains(order.getOrderStatus())) {
            throw new BusinessException(
                    "Restaurant cannot cancel order in status: " + order.getOrderStatus());
        }

        return cancelAndRefundIfNeeded(order);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Đặt orderStatus = CANCELLED; nếu đã thanh toán VIETQR → REFUNDED. (Quy tắc 5.7)
     */
    private OrderResponse cancelAndRefundIfNeeded(CustomerOrder order) {
        order.setOrderStatus(OrderStatus.CANCELLED);

        Optional<Payment> paymentOpt = paymentRepository.findByOrderOrderId(order.getOrderId());
        paymentOpt.ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.PAID
                    && "VIETQR".equals(payment.getPaymentMethod().getMethodCode())) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
        });

        return OrderResponse.from(orderRepository.save(order));
    }

    private CustomerOrder findOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
