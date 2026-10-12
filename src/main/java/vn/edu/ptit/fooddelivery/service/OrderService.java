package vn.edu.ptit.fooddelivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.fooddelivery.config.ShippingProperties;
import vn.edu.ptit.fooddelivery.dto.request.CreateOrderRequest;
import vn.edu.ptit.fooddelivery.dto.response.OrderPreviewResponse;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.entity.Customer;
import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.FoodItem;
import vn.edu.ptit.fooddelivery.entity.Restaurant;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.CustomerRepository;
import vn.edu.ptit.fooddelivery.repository.FoodItemRepository;
import vn.edu.ptit.fooddelivery.repository.RestaurantRepository;
import vn.edu.ptit.fooddelivery.util.ShippingFeeCalculator;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final CustomerRepository       customerRepository;
    private final RestaurantRepository     restaurantRepository;
    private final FoodItemRepository       foodItemRepository;
    private final CustomerOrderRepository  orderRepository;
    private final ShippingFeeCalculator    shippingFeeCalculator;

    // ─── Queries ──────────────────────────────────────────────────────────────

    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(findOrderOrThrow(orderId));
    }

    /**
     * Xem trước phí ship và tổng tiền (không lưu vào DB).
     */
    @Transactional(readOnly = true)
    public OrderPreviewResponse previewOrder(CreateOrderRequest req) {
        Restaurant restaurant = restaurantRepository.findById(req.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + req.restaurantId()));

        int gap = shippingFeeCalculator.calculateGap(restaurant.getDistrictNo(), req.deliveryDistrictNo());
        BigDecimal shippingFee = shippingFeeCalculator.calculateFee(restaurant.getDistrictNo(), req.deliveryDistrictNo());

        BigDecimal itemsTotal = req.items().stream()
                .map(ir -> {
                    FoodItem fi = foodItemRepository.findById(ir.itemId())
                            .orElseThrow(() -> new ResourceNotFoundException("FoodItem not found: " + ir.itemId()));
                    return fi.getPrice().multiply(BigDecimal.valueOf(ir.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderPreviewResponse(
                restaurant.getDistrictNo(),
                req.deliveryDistrictNo(),
                gap,
                shippingFee,
                itemsTotal,
                shippingFee.add(itemsTotal)
        );
    }

    // ─── Commands ─────────────────────────────────────────────────────────────

    /**
     * Tạo đơn hàng mới.
     * <ul>
     *   <li>Kiểm tra khách và nhà hàng tồn tại, nhà hàng active.</li>
     *   <li>Kiểm tra từng món: available=true và thuộc đúng nhà hàng.</li>
     *   <li>Tính shipping_fee theo quận (ShippingFeeCalculator), subtotal, total_amount.</li>
     *   <li>Lưu CustomerOrder kèm OrderItem qua CascadeType.ALL.</li>
     * </ul>
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + req.customerId()));

        Restaurant restaurant = restaurantRepository.findById(req.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found: " + req.restaurantId()));

        if (!restaurant.isActive()) {
            throw new BusinessException("Restaurant is not active: " + req.restaurantId());
        }

        BigDecimal shippingFee = shippingFeeCalculator.calculateFee(restaurant.getDistrictNo(), req.deliveryDistrictNo());

        CustomerOrder order = CustomerOrder.builder()
                .customer(customer)
                .restaurant(restaurant)
                .deliveryDistrictNo(req.deliveryDistrictNo())
                .shippingFee(shippingFee)
                .totalAmount(BigDecimal.ZERO)   // recalculated after adding items
                .deliveryAddress(req.deliveryAddress())
                .build();

        // Thêm từng món – validate và chốt giá
        for (var itemReq : req.items()) {
            FoodItem foodItem = foodItemRepository.findById(itemReq.itemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "FoodItem not found: " + itemReq.itemId()));

            if (!foodItem.isAvailable()) {
                throw new BusinessException("FoodItem is not available: " + itemReq.itemId());
            }
            if (!foodItem.getRestaurant().getRestaurantId().equals(req.restaurantId())) {
                throw new BusinessException(
                        "FoodItem " + itemReq.itemId() + " does not belong to restaurant " + req.restaurantId());
            }

            order.addItem(foodItem, itemReq.quantity());
        }

        order.recalculateTotalAmount();

        return OrderResponse.from(orderRepository.save(order));
    }

    CustomerOrder findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
