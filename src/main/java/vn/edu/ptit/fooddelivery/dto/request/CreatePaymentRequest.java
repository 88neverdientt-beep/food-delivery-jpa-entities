package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "orderId is required")
        Long orderId,

        /** "COD" hoặc "VIETQR" */
        @NotBlank(message = "methodCode is required")
        String methodCode
) {}
