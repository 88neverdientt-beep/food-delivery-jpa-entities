package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long customerId,
        Long restaurantId,
        String restaurantName,
        Integer restaurantDistrictNo,
        Integer deliveryDistrictNo,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        String deliveryAddress,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(CustomerOrder o) {
        List<OrderItemResponse> itemResponses = o.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                o.getOrderId(),
                o.getCustomer().getCustomerId(),
                o.getRestaurant().getRestaurantId(),
                o.getRestaurant().getRestaurantName(),
                o.getRestaurant().getDistrictNo(),
                o.getDeliveryDistrictNo(),
                o.getShippingFee(),
                o.getTotalAmount(),
                o.getOrderStatus(),
                o.getDeliveryAddress(),
                o.getCreatedAt(),
                itemResponses
        );
    }
}
