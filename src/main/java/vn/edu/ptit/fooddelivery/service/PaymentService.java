package vn.edu.ptit.fooddelivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.fooddelivery.dto.request.VietQrWebhookRequest;
import vn.edu.ptit.fooddelivery.dto.response.PaymentResponse;
import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.Payment;
import vn.edu.ptit.fooddelivery.entity.PaymentMethod;
import vn.edu.ptit.fooddelivery.entity.enums.PaymentStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ConflictException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.PaymentMethodRepository;
import vn.edu.ptit.fooddelivery.repository.PaymentRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final String VIETQR_CODE = "VIETQR";

    private final CustomerOrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository       paymentRepository;

    // ─── Commands ─────────────────────────────────────────────────────────────

    /**
     * Tạo bản ghi thanh toán (PENDING) cho đơn hàng.
     * Mỗi đơn chỉ được có một Payment (uk_payments_order).
     */
    @Transactional
    public PaymentResponse createPayment(Long orderId, String methodCode) {
        CustomerOrder order = findOrderOrThrow(orderId);

        if (paymentRepository.findByOrderOrderId(orderId).isPresent()) {
            throw new ConflictException("Payment already exists for order: " + orderId);
        }

        PaymentMethod method = paymentMethodRepository.findByMethodCode(methodCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PaymentMethod not found: " + methodCode));

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(order.getTotalAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    /**
     * Xử lý webhook từ ngân hàng VietQR.
     * Tìm Payment theo orderId, lưu transactionCode, chuyển sang PAID. (Quy tắc 5.4)
     */
    @Transactional
    public PaymentResponse handleVietQrWebhook(VietQrWebhookRequest req) {
        Payment payment = paymentRepository.findByOrderOrderId(req.orderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + req.orderId()));

        if (!VIETQR_CODE.equals(payment.getPaymentMethod().getMethodCode())) {
            throw new BusinessException("Payment method is not VIETQR for order: " + req.orderId());
        }
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Payment is not in PENDING status: " + payment.getPaymentId());
        }

        payment.setTransactionCode(req.transactionCode());
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    /**
     * Hoàn tiền cho đơn hủy đã thanh toán chuyển khoản. (Quy tắc 5.7)
     * Chỉ áp dụng cho VIETQR trạng thái PAID.
     */
    @Transactional
    public PaymentResponse refund(Long orderId) {
        Payment payment = paymentRepository.findByOrderOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + orderId));

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException("Only PAID payments can be refunded.");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private CustomerOrder findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
