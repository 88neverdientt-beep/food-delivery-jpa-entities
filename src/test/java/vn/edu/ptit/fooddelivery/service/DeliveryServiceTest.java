package vn.edu.ptit.fooddelivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.ptit.fooddelivery.dto.response.DeliveryResponse;
import vn.edu.ptit.fooddelivery.entity.*;
import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ConflictException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.DeliveryRepository;
import vn.edu.ptit.fooddelivery.repository.DriverRepository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock DeliveryRepository deliveryRepository;
    @Mock DriverRepository   driverRepository;

    @InjectMocks DeliveryService deliveryService;

    private static final Long ORDER_ID   = 1L;
    private static final Long DRIVER1_ID = 10L;
    private static final Long DRIVER2_ID = 20L;

    private CustomerOrder order;
    private Delivery      deliveryNoDriver;
    private Driver        driver1;
    private Driver        driver2;

    @BeforeEach
    void setUp() {
        order = CustomerOrder.builder()
                .orderId(ORDER_ID)
                .orderStatus(OrderStatus.READY_FOR_PICKUP)
                .build();

        // Delivery chưa có tài xế
        deliveryNoDriver = Delivery.builder()
                .deliveryId(100L)
                .order(order)
                .deliveryStatus(DeliveryStatus.ASSIGNED)
                .build();

        driver1 = Driver.builder()
                .driverId(DRIVER1_ID).fullName("Tài xế 1")
                .online(true).busy(false).build();

        driver2 = Driver.builder()
                .driverId(DRIVER2_ID).fullName("Tài xế 2")
                .online(true).busy(false).build();
    }

    // ─── acceptDelivery – happy path ──────────────────────────────────────────

    @Test
    @DisplayName("acceptDelivery – tài xế đầu tiên nhận thành công")
    void acceptDelivery_firstDriver_succeeds() {
        when(deliveryRepository.findByOrderIdWithLock(ORDER_ID))
                .thenReturn(Optional.of(deliveryNoDriver));
        when(driverRepository.findById(DRIVER1_ID)).thenReturn(Optional.of(driver1));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(driverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse resp = deliveryService.acceptDelivery(ORDER_ID, DRIVER1_ID);

        assertThat(resp.driverId()).isEqualTo(DRIVER1_ID);
        assertThat(resp.deliveryStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        assertThat(driver1.isBusy()).isTrue();
    }

    // ─── acceptDelivery – race condition: hai tài xế cùng nhận ───────────────

    /**
     * Mô phỏng race condition:
     * - Tài xế 1 nhận thành công → delivery.driver được gán.
     * - Tài xế 2 gọi sau: mock trả về delivery đã có driver1.
     * → Tài xế 2 phải nhận ConflictException (như kết quả của khóa bi quan).
     */
    @Test
    @DisplayName("acceptDelivery – tài xế thứ hai bị từ chối (ConflictException)")
    void acceptDelivery_secondDriver_conflict() {
        // Delivery đã có driver1 (sau khi driver1 nhận thành công)
        Delivery deliveryWithDriver1 = Delivery.builder()
                .deliveryId(100L)
                .order(order)
                .driver(driver1)    // đã có chủ
                .deliveryStatus(DeliveryStatus.ASSIGNED)
                .build();

        when(deliveryRepository.findByOrderIdWithLock(ORDER_ID))
                .thenReturn(Optional.of(deliveryWithDriver1));

        assertThatThrownBy(() -> deliveryService.acceptDelivery(ORDER_ID, DRIVER2_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already accepted");

        verify(deliveryRepository, never()).save(any());
    }

    /**
     * Test với AtomicBoolean để mô phỏng: hai luồng gọi tuần tự,
     * luồng đầu thành công, luồng sau thấy delivery đã có driver.
     */
    @Test
    @DisplayName("acceptDelivery – hai tài xế tuần tự: chỉ một người thành công")
    void acceptDelivery_sequential_onlyOneSucceeds() {
        AtomicBoolean firstCall = new AtomicBoolean(true);

        when(deliveryRepository.findByOrderIdWithLock(ORDER_ID)).thenAnswer(inv -> {
            if (firstCall.compareAndSet(true, false)) {
                return Optional.of(deliveryNoDriver);   // lần 1: chưa có driver
            }
            // lần 2: delivery đã có driver1 (kết quả sau khi lần 1 save)
            return Optional.of(Delivery.builder()
                    .deliveryId(100L).order(order)
                    .driver(driver1).deliveryStatus(DeliveryStatus.ASSIGNED).build());
        });

        when(driverRepository.findById(DRIVER1_ID)).thenReturn(Optional.of(driver1));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(driverRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Tài xế 1 thành công
        DeliveryResponse resp = deliveryService.acceptDelivery(ORDER_ID, DRIVER1_ID);
        assertThat(resp.driverId()).isEqualTo(DRIVER1_ID);

        // Tài xế 2 thất bại
        assertThatThrownBy(() -> deliveryService.acceptDelivery(ORDER_ID, DRIVER2_ID))
                .isInstanceOf(ConflictException.class);
    }

    // ─── acceptDelivery – validation ──────────────────────────────────────────

    @Test
    @DisplayName("acceptDelivery – delivery không tồn tại → ResourceNotFoundException")
    void acceptDelivery_deliveryNotFound() {
        when(deliveryRepository.findByOrderIdWithLock(ORDER_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> deliveryService.acceptDelivery(ORDER_ID, DRIVER1_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("acceptDelivery – tài xế offline → BusinessException")
    void acceptDelivery_driverOffline() {
        driver1.setOnline(false);
        when(deliveryRepository.findByOrderIdWithLock(ORDER_ID))
                .thenReturn(Optional.of(deliveryNoDriver));
        when(driverRepository.findById(DRIVER1_ID)).thenReturn(Optional.of(driver1));

        assertThatThrownBy(() -> deliveryService.acceptDelivery(ORDER_ID, DRIVER1_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not online");
    }

    @Test
    @DisplayName("acceptDelivery – tài xế đang bận → BusinessException")
    void acceptDelivery_driverBusy() {
        driver1.setBusy(true);
        when(deliveryRepository.findByOrderIdWithLock(ORDER_ID))
                .thenReturn(Optional.of(deliveryNoDriver));
        when(driverRepository.findById(DRIVER1_ID)).thenReturn(Optional.of(driver1));

        assertThatThrownBy(() -> deliveryService.acceptDelivery(ORDER_ID, DRIVER1_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already busy");
    }
}
