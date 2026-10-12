package vn.edu.ptit.fooddelivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.request.CreatePaymentRequest;
import vn.edu.ptit.fooddelivery.dto.request.VietQrWebhookRequest;
import vn.edu.ptit.fooddelivery.dto.response.PaymentResponse;
import vn.edu.ptit.fooddelivery.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo bản ghi thanh toán cho đơn hàng.
     * POST /api/payments
     * Body: { "orderId": 1, "methodCode": "COD" }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest req) {
        return paymentService.createPayment(req.orderId(), req.methodCode());
    }

    /**
     * Nhận webhook xác nhận thanh toán VietQR từ ngân hàng.
     * POST /api/payments/webhook/vietqr
     * Body: { "transactionCode": "TXN-001", "orderId": 1 }
     */
    @PostMapping("/webhook/vietqr")
    public PaymentResponse vietQrWebhook(@Valid @RequestBody VietQrWebhookRequest req) {
        return paymentService.handleVietQrWebhook(req);
    }
}
