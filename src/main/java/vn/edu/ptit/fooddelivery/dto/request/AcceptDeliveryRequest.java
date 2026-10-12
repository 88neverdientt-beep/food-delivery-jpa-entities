package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotNull;

public record AcceptDeliveryRequest(

        @NotNull(message = "driverId is required")
        Long driverId
) {}
