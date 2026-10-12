package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;

public record UpdateOrderStatusRequest(

        @NotNull(message = "newStatus is required")
        OrderStatus newStatus
) {}
