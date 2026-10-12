package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request tạo đơn hàng mới.
 * Tất cả món phải cùng thuộc một nhà hàng (kiểm tra trong service).
 */
public record CreateOrderRequest(

        @NotNull(message = "customerId is required")
        Long customerId,

        @NotNull(message = "restaurantId is required")
        Long restaurantId,

        @NotBlank(message = "deliveryAddress is required")
        String deliveryAddress,

        @NotNull(message = "deliveryDistrictNo is required")
        Integer deliveryDistrictNo,

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemRequest> items
) {}
