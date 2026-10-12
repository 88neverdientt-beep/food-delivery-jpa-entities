package vn.edu.ptit.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload từ webhook ngân hàng VietQR.
 * {@code transactionCode} – mã giao dịch duy nhất do ngân hàng cấp.
 * {@code orderId}         – ID đơn hàng (nhúng trong phần mô tả chuyển khoản).
 */
public record VietQrWebhookRequest(

        @NotBlank(message = "transactionCode is required")
        String transactionCode,

        @NotNull(message = "orderId is required")
        Long orderId
) {}
