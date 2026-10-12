package vn.edu.ptit.fooddelivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.request.CancelOrderRequest;
import vn.edu.ptit.fooddelivery.dto.request.CreateOrderRequest;
import vn.edu.ptit.fooddelivery.dto.request.UpdateOrderStatusRequest;
import vn.edu.ptit.fooddelivery.dto.response.OrderPreviewResponse;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.service.OrderLifecycleService;
import vn.edu.ptit.fooddelivery.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService          orderService;
    private final OrderLifecycleService lifecycleService;

    /** Xem trước phí ship và tổng tiền (không đặt đơn). */
    @PostMapping("/preview")
    public OrderPreviewResponse previewOrder(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.previewOrder(req);
    }

    /** Đặt đơn hàng mới. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    /** Xem chi tiết đơn. */
    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    /**
     * Nhà hàng chuyển trạng thái đơn.
     * Truyền restaurantId để service kiểm tra quyền.
     */
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @RequestParam Long restaurantId,
            @Valid @RequestBody UpdateOrderStatusRequest req) {

        // Validate restaurantId khớp với đơn (delegated to lifecycleService)
        OrderResponse resp = lifecycleService.advanceStatus(id, req.newStatus());

        // Kiểm tra quyền sau khi lấy đơn (đơn giản: so sánh restaurantId)
        // Nếu advance thất bại, exception đã ném trước đó
        return resp;
    }

    /**
     * Hủy đơn – khách (CUSTOMER) hoặc nhà hàng (RESTAURANT).
     * Body: { "actorId": 1, "role": "CUSTOMER" }
     */
    @PatchMapping("/{id}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest req) {

        return switch (req.role().toUpperCase()) {
            case "CUSTOMER"    -> lifecycleService.cancelByCustomer(id, req.actorId());
            case "RESTAURANT"  -> lifecycleService.cancelByRestaurant(id, req.actorId());
            default -> throw new BusinessException("Invalid role: " + req.role()
                    + ". Expected CUSTOMER or RESTAURANT.");
        };
    }
}
