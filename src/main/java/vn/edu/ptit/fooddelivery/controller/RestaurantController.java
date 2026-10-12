package vn.edu.ptit.fooddelivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.dto.response.RestaurantResponse;
import vn.edu.ptit.fooddelivery.dto.response.FoodItemResponse;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.FoodItemRepository;
import vn.edu.ptit.fooddelivery.repository.RestaurantRepository;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository    restaurantRepository;
    private final FoodItemRepository      foodItemRepository;
    private final CustomerOrderRepository orderRepository;

    /** Danh sách tất cả nhà hàng đang hoạt động. */
    @GetMapping
    public List<RestaurantResponse> getAllActive() {
        return restaurantRepository.findAllByActiveTrue().stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    /** Danh sách món đang bán của nhà hàng. */
    @GetMapping("/{id}/items")
    public List<FoodItemResponse> getAvailableItems(@PathVariable Long id) {
        return foodItemRepository.findAvailableByRestaurant(id).stream()
                .map(FoodItemResponse::from)
                .toList();
    }

    /** Đơn hàng của nhà hàng (theo restaurantId). */
    @GetMapping("/{id}/orders")
    public List<OrderResponse> getOrders(@PathVariable Long id) {
        restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
        return orderRepository.findAll().stream()
                .filter(o -> o.getRestaurant().getRestaurantId().equals(id))
                .map(OrderResponse::from)
                .toList();
    }
}
