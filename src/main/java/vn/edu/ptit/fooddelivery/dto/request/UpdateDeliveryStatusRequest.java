package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotNull;
import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;

public record UpdateDeliveryStatusRequest(

        @NotNull(message = "newStatus is required")
        DeliveryStatus newStatus
) {}
