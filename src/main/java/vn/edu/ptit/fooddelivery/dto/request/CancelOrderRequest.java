package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dùng cho PATCH /api/orders/{id}/cancel.
 * {@code role} là "CUSTOMER" hoặc "RESTAURANT".
 */
public record CancelOrderRequest(

        @NotNull(message = "actorId is required")
        Long actorId,

        @NotBlank(message = "role is required (CUSTOMER or RESTAURANT)")
        String role
) {}
