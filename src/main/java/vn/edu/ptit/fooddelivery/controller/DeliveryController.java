package vn.edu.ptit.fooddelivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.request.AcceptDeliveryRequest;
import vn.edu.ptit.fooddelivery.dto.request.UpdateDeliveryStatusRequest;
import vn.edu.ptit.fooddelivery.dto.response.DeliveryResponse;
import vn.edu.ptit.fooddelivery.service.DeliveryService;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * Tài xế nhận đơn (first-come, first-served với khóa bi quan).
     * POST /api/deliveries/{orderId}/accept
     * Body: { "driverId": 1 }
     */
    @PostMapping("/{orderId}/accept")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse acceptDelivery(
            @PathVariable Long orderId,
            @Valid @RequestBody AcceptDeliveryRequest req) {
        return deliveryService.acceptDelivery(orderId, req.driverId());
    }

    /**
     * Tài xế cập nhật trạng thái giao hàng.
     * PATCH /api/deliveries/{id}/status
     * Body: { "newStatus": "PICKED_UP" }
     */
    @PatchMapping("/{id}/status")
    public DeliveryResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeliveryStatusRequest req) {
        return deliveryService.updateStatus(id, req.newStatus());
    }
}
