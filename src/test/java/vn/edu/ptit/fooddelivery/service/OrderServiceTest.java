package vn.edu.ptit.fooddelivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.ptit.fooddelivery.config.ShippingProperties;
import vn.edu.ptit.fooddelivery.dto.request.CreateOrderRequest;
import vn.edu.ptit.fooddelivery.dto.request.OrderItemRequest;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.entity.*;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock CustomerRepository      customerRepository;
    @Mock RestaurantRepository    restaurantRepository;
    @Mock FoodItemRepository      foodItemRepository;
    @Mock CustomerOrderRepository orderRepository;

    @InjectMocks OrderService orderService;

    // ShippingProperties không inject được qua @InjectMocks vì là record
    private final ShippingProperties shipping =
            new ShippingProperties(10, java.math.BigDecimal.valueOf(15_000), java.math.BigDecimal.valueOf(15_000));

    private Customer    customer;
    private Restaurant  restaurant;
    private FoodItem    foodItem;

    @BeforeEach
    void setUp() {
        // Inject ShippingProperties thủ công
        orderService = new OrderService(
                customerRepository, restaurantRepository,
                foodItemRepository, orderRepository, shipping);

        customer = Customer.builder()
                .customerId(1L).fullName("Test Customer")
                .email("c@test.com").phoneNumber("0900000001").passwordHash("h").build();

        restaurant = Restaurant.builder()
                .restaurantId(1L).restaurantName("Test Restaurant")
                .email("r@test.com").passwordHash("h").phoneNumber("0900000002")
                .address("addr").latitude(21.0).longitude(105.8).active(true).build();

        foodItem = FoodItem.builder()
                .itemId(10L).itemName("Phở bò").price(new BigDecimal("55000"))
                .available(true).restaurant(restaurant).build();
    }

    // ─── createOrder – happy path ─────────────────────────────────────────────

    @Test
    @DisplayName("createOrder – tạo đơn thành công, total_amount đúng")
    void createOrder_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            CustomerOrder o = inv.getArgument(0);
            // simulate ID assignment
            return CustomerOrder.builder()
                    .orderId(100L)
                    .customer(o.getCustomer())
                    .restaurant(o.getRestaurant())
                    .distanceKm(o.getDistanceKm())
                    .shippingFee(o.getShippingFee())
                    .totalAmount(o.getTotalAmount())
                    .orderStatus(OrderStatus.PENDING)
                    .deliveryAddress(o.getDeliveryAddress())
                    .deliveryLatitude(o.getDeliveryLatitude())
                    .deliveryLongitude(o.getDeliveryLongitude())
                    .build();
        });

        CreateOrderRequest req = new CreateOrderRequest(
                1L, 1L,
                "Delivery Address", 21.01, 105.81,
                List.of(new OrderItemRequest(10L, 2)));

        OrderResponse resp = orderService.createOrder(req);

        assertThat(resp.orderId()).isEqualTo(100L);
        assertThat(resp.orderStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("createOrder – customer không tồn tại → ResourceNotFoundException")
    void createOrder_customerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        CreateOrderRequest req = new CreateOrderRequest(
                99L, 1L, "addr", 21.0, 105.8,
                List.of(new OrderItemRequest(10L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    @DisplayName("createOrder – nhà hàng inactive → BusinessException")
    void createOrder_restaurantInactive() {
        restaurant.setActive(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        CreateOrderRequest req = new CreateOrderRequest(
                1L, 1L, "addr", 21.0, 105.8,
                List.of(new OrderItemRequest(10L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("createOrder – món unavailable → BusinessException")
    void createOrder_itemUnavailable() {
        foodItem.setAvailable(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));

        CreateOrderRequest req = new CreateOrderRequest(
                1L, 1L, "addr", 21.0, 105.8,
                List.of(new OrderItemRequest(10L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("createOrder – món thuộc nhà hàng khác → BusinessException")
    void createOrder_itemBelongsToDifferentRestaurant() {
        Restaurant other = Restaurant.builder().restaurantId(99L).build();
        foodItem.setRestaurant(other);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));

        CreateOrderRequest req = new CreateOrderRequest(
                1L, 1L, "addr", 21.0, 105.8,
                List.of(new OrderItemRequest(10L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not belong to restaurant");
    }

    // ─── computeShippingFee ────────────────────────────────────────────────────

    @Test
    @DisplayName("computeShippingFee – khoảng cách <= base_km → trả base_fee")
    void computeShippingFee_withinBaseKm() {
        BigDecimal fee = orderService.computeShippingFee(1.5); // base_km=2
        assertThat(fee).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("computeShippingFee – khoảng cách 5km → base + 3*5000 = 30000")
    void computeShippingFee_overBaseKm() {
        BigDecimal fee = orderService.computeShippingFee(5.0);
        assertThat(fee).isEqualByComparingTo("30000.00");
    }

    @Test
    @DisplayName("computeShippingFee – khoảng cách rất xa → không vượt max_fee")
    void computeShippingFee_capped() {
        BigDecimal fee = orderService.computeShippingFee(100.0);
        assertThat(fee).isEqualByComparingTo("50000.00");
    }
}
