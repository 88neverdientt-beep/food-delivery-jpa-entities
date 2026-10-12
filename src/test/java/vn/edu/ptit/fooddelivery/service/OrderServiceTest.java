package vn.edu.ptit.fooddelivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.ptit.fooddelivery.dto.request.CreateOrderRequest;
import vn.edu.ptit.fooddelivery.dto.request.OrderItemRequest;
import vn.edu.ptit.fooddelivery.dto.response.OrderPreviewResponse;
import vn.edu.ptit.fooddelivery.dto.response.OrderResponse;
import vn.edu.ptit.fooddelivery.entity.Customer;
import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.FoodItem;
import vn.edu.ptit.fooddelivery.entity.Restaurant;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.CustomerRepository;
import vn.edu.ptit.fooddelivery.repository.FoodItemRepository;
import vn.edu.ptit.fooddelivery.repository.RestaurantRepository;
import vn.edu.ptit.fooddelivery.util.ShippingFeeCalculator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock CustomerRepository      customerRepository;
    @Mock RestaurantRepository    restaurantRepository;
    @Mock FoodItemRepository      foodItemRepository;
    @Mock CustomerOrderRepository orderRepository;

    @InjectMocks OrderService orderService;

    private final ShippingFeeCalculator shippingFeeCalculator =
            new ShippingFeeCalculator(10, BigDecimal.valueOf(15_000), BigDecimal.valueOf(15_000));

    private Customer    customer;
    private Restaurant  restaurant;
    private FoodItem    foodItem;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                customerRepository, restaurantRepository,
                foodItemRepository, orderRepository, shippingFeeCalculator);

        customer = Customer.builder()
                .customerId(1L).fullName("Test Customer")
                .email("c@test.com").phoneNumber("0900000001").passwordHash("h").build();

        restaurant = Restaurant.builder()
                .restaurantId(1L).restaurantName("Test Restaurant")
                .email("r@test.com").passwordHash("h").phoneNumber("0900000002")
                .address("addr").districtNo(1).active(true).build();

        foodItem = FoodItem.builder()
                .itemId(10L).itemName("Phở bò").price(new BigDecimal("55000"))
                .available(true).restaurant(restaurant).build();
    }

    // ─── previewOrder ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("previewOrder – xem trước phí ship và tổng tiền chính xác theo quận")
    void previewOrder_success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));

        // Nhà hàng quận 1, giao quận 3: gap = 2 => phí ship = 30000, 2 bát phở = 110000 => tổng = 140000
        CreateOrderRequest req = new CreateOrderRequest(
                1L, 1L,
                "Delivery Address", 3,
                List.of(new OrderItemRequest(10L, 2)));

        OrderPreviewResponse resp = orderService.previewOrder(req);

        assertThat(resp.restaurantDistrictNo()).isEqualTo(1);
        assertThat(resp.deliveryDistrictNo()).isEqualTo(3);
        assertThat(resp.gap()).isEqualTo(2);
        assertThat(resp.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(30000));
        assertThat(resp.itemsTotal()).isEqualByComparingTo(BigDecimal.valueOf(110000));
        assertThat(resp.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(140000));
    }

    // ─── createOrder – happy path ─────────────────────────────────────────────

    @Test
    @DisplayName("createOrder – tạo đơn thành công, lưu đúng quận và phí ship")
    void createOrder_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            CustomerOrder o = inv.getArgument(0);
            return CustomerOrder.builder()
                    .orderId(100L)
                    .customer(o.getCustomer())
                    .restaurant(o.getRestaurant())
                    .deliveryDistrictNo(o.getDeliveryDistrictNo())
                    .shippingFee(o.getShippingFee())
                    .totalAmount(o.getTotalAmount())
                    .orderStatus(OrderStatus.PENDING)
                    .deliveryAddress(o.getDeliveryAddress())
                    .build();
        });

        // Nhà hàng quận 1, giao quận 2: gap = 1 => phí = 15000, 2 món = 110000 => tổng = 125000
        CreateOrderRequest req = new CreateOrderRequest(
                1L, 1L,
                "Delivery Address", 2,
                List.of(new OrderItemRequest(10L, 2)));

        OrderResponse resp = orderService.createOrder(req);

        assertThat(resp.orderId()).isEqualTo(100L);
        assertThat(resp.restaurantDistrictNo()).isEqualTo(1);
        assertThat(resp.deliveryDistrictNo()).isEqualTo(2);
        assertThat(resp.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(resp.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(125000));
        assertThat(resp.orderStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("createOrder – customer không tồn tại → ResourceNotFoundException")
    void createOrder_customerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        CreateOrderRequest req = new CreateOrderRequest(
                99L, 1L, "addr", 1,
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
                1L, 1L, "addr", 1,
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
                1L, 1L, "addr", 1,
                List.of(new OrderItemRequest(10L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("createOrder – món thuộc nhà hàng khác → BusinessException")
    void createOrder_itemBelongsToDifferentRestaurant() {
        Restaurant other = Restaurant.builder().restaurantId(99L).districtNo(2).build();
        foodItem.setRestaurant(other);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(foodItem));

        CreateOrderRequest req = new CreateOrderRequest(
                1L, 1L, "addr", 1,
                List.of(new OrderItemRequest(10L, 1)));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not belong to restaurant");
    }
}
