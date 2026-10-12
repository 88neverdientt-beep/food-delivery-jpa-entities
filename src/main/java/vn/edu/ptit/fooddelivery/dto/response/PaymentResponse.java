package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.Payment;
import vn.edu.ptit.fooddelivery.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        String methodCode,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        String transactionCode,
        LocalDateTime paidAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getPaymentId(),
                p.getOrder().getOrderId(),
                p.getPaymentMethod().getMethodCode(),
                p.getAmount(),
                p.getPaymentStatus(),
                p.getTransactionCode(),
                p.getPaidAt()
        );
    }
}
