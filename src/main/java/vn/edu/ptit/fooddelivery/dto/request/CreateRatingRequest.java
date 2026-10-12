package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateRatingRequest(

        @NotNull(message = "customerId is required")
        Long customerId,

        @Min(value = 1, message = "ratingScore minimum is 1")
        @Max(value = 5, message = "ratingScore maximum is 5")
        int ratingScore,

        /** Tùy chọn, tối đa 1 000 ký tự (khớp với DB). */
        String comment
) {}
