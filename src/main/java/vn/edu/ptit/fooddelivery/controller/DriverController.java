package vn.edu.ptit.fooddelivery.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.response.DeliveryResponse;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.entity.Driver;
import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.DriverRepository;
import vn.edu.ptit.fooddelivery.service.DeliveryService;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverRepository        driverRepository;
    private final CustomerOrderRepository orderRepository;
    private final DeliveryService         deliveryService;

    record ToggleOnlineRequest(@NotNull Boolean online) {}
    record UpdateLocationRequest(@NotNull Double latitude, @NotNull Double longitude) {}

    /** Công tắc online / offline. */
    @PatchMapping("/{id}/online")
    public Driver toggleOnline(@PathVariable Long id,
                               @Valid @RequestBody ToggleOnlineRequest req) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        driver.setOnline(req.online());
        return driverRepository.save(driver);
    }

    /** Cập nhật GPS vị trí hiện tại. */
    @PatchMapping("/{id}/location")
    public Driver updateLocation(@PathVariable Long id,
                                 @Valid @RequestBody UpdateLocationRequest req) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        driver.setCurrentLatitude(req.latitude());
        driver.setCurrentLongitude(req.longitude());
        return driverRepository.save(driver);
    }

    /** Danh sách đơn chờ tài xế (READY_FOR_PICKUP, chưa có driver), hỗ trợ lọc quận và phân trang. */
    @GetMapping("/deliveries/available")
    public Page<OrderResponse> getAvailableOrders(
            @RequestParam(required = false) Integer district,
            @RequestParam(required = false) Integer districtNo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Integer targetDistrict = district != null ? district : districtNo;
        return orderRepository.findAwaitingDriver(OrderStatus.READY_FOR_PICKUP, targetDistrict, pageable)
                .map(OrderResponse::from);
    }

    /** Đơn tài xế đang giao (delivery status ASSIGNED hoặc PICKED_UP). */
    @GetMapping("/{id}/active-delivery")
    public List<DeliveryResponse> getActiveDelivery(@PathVariable Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + id));
        return driver.getDeliveries().stream()
                .filter(d -> d.getDeliveryStatus() == DeliveryStatus.ASSIGNED
                          || d.getDeliveryStatus() == DeliveryStatus.PICKED_UP)
                .map(DeliveryResponse::from)
                .toList();
    }
}
