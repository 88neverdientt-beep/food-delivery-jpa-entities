package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long itemId,
        String itemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem oi) {
        return new OrderItemResponse(
                oi.getFoodItem().getItemId(),
                oi.getFoodItem().getItemName(),
                oi.getQuantity(),
                oi.getUnitPrice(),
                oi.getSubtotal()
        );
    }
}
