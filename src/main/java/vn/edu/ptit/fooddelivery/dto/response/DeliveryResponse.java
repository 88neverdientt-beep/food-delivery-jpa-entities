package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.Delivery;
import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long deliveryId,
        Long orderId,
        Long driverId,
        String driverName,
        DeliveryStatus deliveryStatus,
        LocalDateTime pickupTime,
        LocalDateTime deliveredTime
) {
    public static DeliveryResponse from(Delivery d) {
        Long driverId   = d.getDriver() != null ? d.getDriver().getDriverId() : null;
        String driverName = d.getDriver() != null ? d.getDriver().getFullName() : null;

        return new DeliveryResponse(
                d.getDeliveryId(),
                d.getOrder().getOrderId(),
                driverId,
                driverName,
                d.getDeliveryStatus(),
                d.getPickupTime(),
                d.getDeliveredTime()
        );
    }
}
