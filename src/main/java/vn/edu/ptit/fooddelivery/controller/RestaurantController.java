package vn.edu.ptit.fooddelivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.response.FoodItemResponse;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.dto.response.RestaurantResponse;
import vn.edu.ptit.fooddelivery.entity.Restaurant;
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

    /**
     * Danh sách nhà hàng đang hoạt động, hỗ trợ lọc theo quận và phân trang.
     */
    @GetMapping
    public Page<RestaurantResponse> getAllActive(
            @RequestParam(required = false) Integer district,
            @RequestParam(required = false) Integer districtNo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Integer targetDistrict = district != null ? district : districtNo;
        Page<Restaurant> page = (targetDistrict != null)
                ? restaurantRepository.findAllByActiveTrueAndDistrictNo(targetDistrict, pageable)
                : restaurantRepository.findAllByActiveTrue(pageable);
        return page.map(RestaurantResponse::from);
    }

    /**
     * Thông tin chi tiết một nhà hàng.
     */
    @GetMapping("/{id}")
    public RestaurantResponse getById(@PathVariable Long id) {
        return restaurantRepository.findById(id)
                .map(RestaurantResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
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
