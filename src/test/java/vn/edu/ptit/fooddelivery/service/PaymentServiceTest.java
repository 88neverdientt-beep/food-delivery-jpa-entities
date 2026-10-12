package vn.edu.ptit.fooddelivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.ptit.fooddelivery.dto.request.VietQrWebhookRequest;
import vn.edu.ptit.fooddelivery.dto.response.PaymentResponse;
import vn.edu.ptit.fooddelivery.entity.*;
import vn.edu.ptit.fooddelivery.entity.enums.PaymentStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ConflictException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.PaymentMethodRepository;
import vn.edu.ptit.fooddelivery.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock CustomerOrderRepository orderRepository;
    @Mock PaymentMethodRepository paymentMethodRepository;
    @Mock PaymentRepository       paymentRepository;

    @InjectMocks PaymentService paymentService;

    private CustomerOrder order;
    private PaymentMethod codMethod;
    private PaymentMethod vietqrMethod;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder().customerId(1L).build();
        Restaurant restaurant = Restaurant.builder().restaurantId(1L).build();

        order = CustomerOrder.builder()
                .orderId(1L).customer(customer).restaurant(restaurant)
                .totalAmount(new BigDecimal("100000")).build();

        codMethod    = PaymentMethod.builder().methodId(1L).methodCode("COD").methodName("Tiền mặt").build();
        vietqrMethod = PaymentMethod.builder().methodId(2L).methodCode("VIETQR").methodName("VietQR").build();
    }

    // ─── createPayment ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createPayment – tạo Payment COD thành công")
    void createPayment_cod_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.empty());
        when(paymentMethodRepository.findByMethodCode("COD")).thenReturn(Optional.of(codMethod));
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p = Payment.builder().paymentId(10L).order(p.getOrder())
                    .paymentMethod(p.getPaymentMethod()).amount(p.getAmount())
                    .paymentStatus(p.getPaymentStatus()).build();
            return p;
        });

        PaymentResponse resp = paymentService.createPayment(1L, "COD");

        assertThat(resp.methodCode()).isEqualTo("COD");
        assertThat(resp.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(resp.amount()).isEqualByComparingTo("100000");
    }

    @Test
    @DisplayName("createPayment – đã tồn tại Payment → ConflictException")
    void createPayment_alreadyExists() {
        Payment existing = Payment.builder().paymentId(5L).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> paymentService.createPayment(1L, "COD"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    // ─── handleVietQrWebhook ──────────────────────────────────────────────────

    @Test
    @DisplayName("handleVietQrWebhook – chuyển PENDING → PAID, lưu transactionCode")
    void handleVietQrWebhook_success() {
        Payment pending = Payment.builder()
                .paymentId(20L).order(order).paymentMethod(vietqrMethod)
                .amount(new BigDecimal("100000")).paymentStatus(PaymentStatus.PENDING).build();

        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.of(pending));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse resp = paymentService.handleVietQrWebhook(
                new VietQrWebhookRequest("TXN-001", 1L));

        assertThat(resp.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(resp.transactionCode()).isEqualTo("TXN-001");
        assertThat(resp.paidAt()).isNotNull();
    }

    @Test
    @DisplayName("handleVietQrWebhook – phương thức COD → BusinessException")
    void handleVietQrWebhook_wrongMethod() {
        Payment codPayment = Payment.builder()
                .paymentId(21L).order(order).paymentMethod(codMethod)
                .amount(new BigDecimal("100000")).paymentStatus(PaymentStatus.PENDING).build();

        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.of(codPayment));

        assertThatThrownBy(() -> paymentService.handleVietQrWebhook(
                new VietQrWebhookRequest("TXN-002", 1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not VIETQR");
    }

    // ─── refund ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("refund – PAID → REFUNDED thành công")
    void refund_success() {
        Payment paid = Payment.builder()
                .paymentId(30L).order(order).paymentMethod(vietqrMethod)
                .amount(new BigDecimal("100000")).paymentStatus(PaymentStatus.PAID).build();

        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.of(paid));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse resp = paymentService.refund(1L);

        assertThat(resp.paymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("refund – payment chưa PAID → BusinessException")
    void refund_notPaid() {
        Payment pending = Payment.builder()
                .paymentId(31L).order(order).paymentMethod(vietqrMethod)
                .amount(new BigDecimal("100000")).paymentStatus(PaymentStatus.PENDING).build();

        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> paymentService.refund(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PAID payments");
    }

    @Test
    @DisplayName("refund – không tìm thấy payment → ResourceNotFoundException")
    void refund_notFound() {
        when(paymentRepository.findByOrderOrderId(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentService.refund(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
