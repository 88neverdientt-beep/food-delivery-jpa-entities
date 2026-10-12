package vn.edu.ptit.fooddelivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerOrderRepository orderRepository;

    /** Lịch sử đơn của một khách hàng. */
    @GetMapping("/{id}/orders")
    public List<OrderResponse> getMyOrders(@PathVariable Long id) {
        return orderRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(id).stream()
                .map(OrderResponse::from)
                .toList();
    }
}
