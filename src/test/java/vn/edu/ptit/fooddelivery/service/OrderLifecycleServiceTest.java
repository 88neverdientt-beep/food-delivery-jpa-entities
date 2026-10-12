package vn.edu.ptit.fooddelivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.entity.*;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.entity.enums.PaymentStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.DeliveryRepository;
import vn.edu.ptit.fooddelivery.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleServiceTest {

    @Mock CustomerOrderRepository orderRepository;
    @Mock DeliveryRepository      deliveryRepository;
    @Mock PaymentRepository       paymentRepository;

    @InjectMocks OrderLifecycleService lifecycleService;

    private Customer    customer;
    private Restaurant  restaurant;
    private CustomerOrder pendingOrder;

    @BeforeEach
    void setUp() {
        customer   = Customer.builder().customerId(1L).build();
        restaurant = Restaurant.builder().restaurantId(1L).build();

        pendingOrder = CustomerOrder.builder()
                .orderId(1L)
                .customer(customer)
                .restaurant(restaurant)
                .orderStatus(OrderStatus.PENDING)
                .deliveryDistrictNo(1)
                .shippingFee(new BigDecimal("20000"))
                .totalAmount(new BigDecimal("120000"))
                .deliveryAddress("addr")
                .build();
    }

    // ─── advanceStatus ────────────────────────────────────────────────────────

    @Test
    @DisplayName("advanceStatus – PENDING → CONFIRMED hợp lệ")
    void advanceStatus_pendingToConfirmed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse resp = lifecycleService.advanceStatus(1L, OrderStatus.CONFIRMED);

        assertThat(resp.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("advanceStatus – PENDING → PREPARING không hợp lệ → BusinessException")
    void advanceStatus_invalidTransition() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> lifecycleService.advanceStatus(1L, OrderStatus.PREPARING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot advance");
    }

    @Test
    @DisplayName("advanceStatus – PREPARING → READY_FOR_PICKUP tạo Delivery record")
    void advanceStatus_preparingToReadyForPickup_createsDelivery() {
        pendingOrder.setOrderStatus(OrderStatus.PREPARING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(deliveryRepository.findByOrderOrderId(1L)).thenReturn(Optional.empty());
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        lifecycleService.advanceStatus(1L, OrderStatus.READY_FOR_PICKUP);

        verify(deliveryRepository).save(any(Delivery.class));
    }

    // ─── cancelByCustomer ─────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelByCustomer – PENDING → CANCELLED thành công")
    void cancelByCustomer_pending_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse resp = lifecycleService.cancelByCustomer(1L, 1L);

        assertThat(resp.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelByCustomer – CONFIRMED → BusinessException (chỉ PENDING)")
    void cancelByCustomer_notPending() {
        pendingOrder.setOrderStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> lifecycleService.cancelByCustomer(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("cancelByCustomer – khách sai → BusinessException")
    void cancelByCustomer_wrongCustomer() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> lifecycleService.cancelByCustomer(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not belong to customer");
    }

    // ─── cancelByRestaurant ───────────────────────────────────────────────────

    @Test
    @DisplayName("cancelByRestaurant – CONFIRMED → CANCELLED thành công")
    void cancelByRestaurant_confirmed_success() {
        pendingOrder.setOrderStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse resp = lifecycleService.cancelByRestaurant(1L, 1L);

        assertThat(resp.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelByRestaurant – READY_FOR_PICKUP → BusinessException (đã qua giới hạn)")
    void cancelByRestaurant_readyForPickup_denied() {
        pendingOrder.setOrderStatus(OrderStatus.READY_FOR_PICKUP);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> lifecycleService.cancelByRestaurant(1L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    // ─── Hoàn tiền tự động ────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelByCustomer – đơn đã PAID VIETQR → tự động REFUNDED")
    void cancelByCustomer_paidVietqr_autoRefund() {
        PaymentMethod vietqr = PaymentMethod.builder()
                .methodCode("VIETQR").methodName("VietQR").build();
        Payment paidPayment = Payment.builder()
                .paymentId(5L).order(pendingOrder).paymentMethod(vietqr)
                .amount(new BigDecimal("120000")).paymentStatus(PaymentStatus.PAID).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.of(paidPayment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        lifecycleService.cancelByCustomer(1L, 1L);

        assertThat(paidPayment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(paidPayment);
    }

    @Test
    @DisplayName("cancelByCustomer – đơn COD PAID → không REFUNDED")
    void cancelByCustomer_paidCod_noRefund() {
        PaymentMethod cod = PaymentMethod.builder()
                .methodCode("COD").methodName("Tiền mặt").build();
        Payment codPayment = Payment.builder()
                .paymentId(6L).order(pendingOrder).paymentMethod(cod)
                .amount(new BigDecimal("120000")).paymentStatus(PaymentStatus.PAID).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(paymentRepository.findByOrderOrderId(1L)).thenReturn(Optional.of(codPayment));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        lifecycleService.cancelByCustomer(1L, 1L);

        assertThat(codPayment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID); // không đổi
        verify(paymentRepository, never()).save(any());
    }
}
