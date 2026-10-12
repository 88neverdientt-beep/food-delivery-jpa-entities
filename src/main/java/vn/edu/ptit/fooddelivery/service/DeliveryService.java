package vn.edu.ptit.fooddelivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.fooddelivery.dto.response.DeliveryResponse;
import vn.edu.ptit.fooddelivery.entity.Delivery;
import vn.edu.ptit.fooddelivery.entity.Driver;
import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ConflictException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.DeliveryRepository;
import vn.edu.ptit.fooddelivery.repository.DriverRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository        deliveryRepository;
    private final DriverRepository           driverRepository;
    private final CustomerOrderRepository    orderRepository;

    // ─── Commands ─────────────────────────────────────────────────────────────

    /**
     * Tài xế nhận đơn – first-come, first-served với khóa bi quan. (Quy tắc 5.5)
     * <ol>
     *   <li>Lấy Delivery theo orderId với PESSIMISTIC_WRITE (SELECT … FOR UPDATE).</li>
     *   <li>Nếu đã có tài xế → 409 Conflict (tài xế khác đã nhận trước).</li>
     *   <li>Kiểm tra tài xế online và không bận.</li>
     *   <li>Gán tài xế, đánh dấu is_busy = true.</li>
     * </ol>
     * Phải được gọi trong ngữ cảnh @Transactional để lock có hiệu lực.
     */
    @Transactional
    public DeliveryResponse acceptDelivery(Long orderId, Long driverId) {
        // Khóa bi quan – chặn transaction đồng thời khác đọc/sửa cùng row
        Delivery delivery = deliveryRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found for order: " + orderId));

        // Sau khi lock: kiểm tra lại trạng thái (phòng race condition)
        if (delivery.getDriver() != null) {
            throw new ConflictException(
                    "Order " + orderId + " already accepted by driver "
                    + delivery.getDriver().getDriverId());
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));

        if (!driver.isOnline()) {
            throw new BusinessException("Driver is not online: " + driverId);
        }
        if (driver.isBusy()) {
            throw new BusinessException("Driver is already busy: " + driverId);
        }

        delivery.setDriver(driver);
        delivery.setDeliveryStatus(DeliveryStatus.ASSIGNED);

        driver.setBusy(true);
        driverRepository.save(driver);

        return DeliveryResponse.from(deliveryRepository.save(delivery));
    }

    /**
     * Cập nhật trạng thái delivery (tài xế báo cáo hành trình).
     * ASSIGNED → PICKED_UP → DELIVERED (hoặc FAILED).
     */
    @Transactional
    public DeliveryResponse updateStatus(Long deliveryId, DeliveryStatus newStatus) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryId));

        delivery.setDeliveryStatus(newStatus);

        switch (newStatus) {
            case PICKED_UP -> {
                delivery.setPickupTime(LocalDateTime.now());
                // Đồng bộ: đơn hàng chuyển sang DELIVERING
                delivery.getOrder().setOrderStatus(OrderStatus.DELIVERING);
                orderRepository.save(delivery.getOrder());
            }
            case DELIVERED -> {
                delivery.setDeliveredTime(LocalDateTime.now());
                // Đồng bộ: đơn hàng hoàn tất
                delivery.getOrder().setOrderStatus(OrderStatus.COMPLETED);
                orderRepository.save(delivery.getOrder());
                // Giải phóng tài xế
                if (delivery.getDriver() != null) {
                    delivery.getDriver().setBusy(false);
                    driverRepository.save(delivery.getDriver());
                }
            }
            case FAILED -> {
                delivery.setDeliveredTime(LocalDateTime.now());
                delivery.getOrder().setOrderStatus(OrderStatus.CANCELLED);
                orderRepository.save(delivery.getOrder());
                if (delivery.getDriver() != null) {
                    delivery.getDriver().setBusy(false);
                    driverRepository.save(delivery.getDriver());
                }
            }
            default -> { /* ASSIGNED – không thay đổi order status */ }
        }

        return DeliveryResponse.from(deliveryRepository.save(delivery));
    }
}
