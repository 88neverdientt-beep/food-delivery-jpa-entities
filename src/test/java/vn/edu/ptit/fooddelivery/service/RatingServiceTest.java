package vn.edu.ptit.fooddelivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.ptit.fooddelivery.dto.request.CreateRatingRequest;
import vn.edu.ptit.fooddelivery.dto.response.RatingResponse;
import vn.edu.ptit.fooddelivery.entity.*;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ConflictException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.FoodRatingRepository;
import vn.edu.ptit.fooddelivery.repository.OrderItemRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock CustomerOrderRepository orderRepository;
    @Mock OrderItemRepository     orderItemRepository;
    @Mock FoodRatingRepository    ratingRepository;

    @InjectMocks RatingService ratingService;

    private static final Long ORDER_ID = 1L;
    private static final Long ITEM_ID  = 10L;
    private static final Long CUST_ID  = 5L;

    private CustomerOrder completedOrder;
    private OrderItem     orderItem;
    private FoodItem      foodItem;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder().customerId(CUST_ID).build();
        Restaurant restaurant = Restaurant.builder().restaurantId(1L).build();

        completedOrder = CustomerOrder.builder()
                .orderId(ORDER_ID)
                .customer(customer)
                .restaurant(restaurant)
                .orderStatus(OrderStatus.COMPLETED)
                .deliveryDistrictNo(1)
                .shippingFee(new BigDecimal("15000"))
                .totalAmount(new BigDecimal("70000"))
                .deliveryAddress("addr")
                .build();

        foodItem = FoodItem.builder()
                .itemId(ITEM_ID).itemName("Phở bò")
                .price(new BigDecimal("55000")).available(true)
                .restaurant(restaurant).build();

        orderItem = OrderItem.builder()
                .id(new OrderItemId(ORDER_ID, ITEM_ID))
                .order(completedOrder)
                .foodItem(foodItem)
                .quantity(1)
                .unitPrice(new BigDecimal("55000"))
                .subtotal(new BigDecimal("55000"))
                .build();
    }

    // ─── createRating – happy path ────────────────────────────────────────────

    @Test
    @DisplayName("createRating – đánh giá hợp lệ cho đơn COMPLETED")
    void createRating_success() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(completedOrder));
        when(orderItemRepository.findById(new OrderItemId(ORDER_ID, ITEM_ID)))
                .thenReturn(Optional.of(orderItem));
        when(ratingRepository.save(any())).thenAnswer(inv -> {
            FoodRating r = inv.getArgument(0);
            return FoodRating.builder()
                    .ratingId(99L)
                    .orderItem(r.getOrderItem())
                    .ratingScore(r.getRatingScore())
                    .comment(r.getComment())
                    .build();
        });

        RatingResponse resp = ratingService.createRating(ORDER_ID, ITEM_ID,
                new CreateRatingRequest(CUST_ID, 5, "Rất ngon!"));

        assertThat(resp.ratingScore()).isEqualTo(5);
        assertThat(resp.comment()).isEqualTo("Rất ngon!");
        assertThat(resp.itemId()).isEqualTo(ITEM_ID);
    }

    // ─── createRating – validation ────────────────────────────────────────────

    @Test
    @DisplayName("createRating – đơn chưa COMPLETED → BusinessException")
    void createRating_orderNotCompleted() {
        completedOrder.setOrderStatus(OrderStatus.DELIVERING);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(completedOrder));

        assertThatThrownBy(() -> ratingService.createRating(ORDER_ID, ITEM_ID,
                new CreateRatingRequest(CUST_ID, 4, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("createRating – khách không phải chủ đơn → BusinessException")
    void createRating_wrongCustomer() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(completedOrder));

        assertThatThrownBy(() -> ratingService.createRating(ORDER_ID, ITEM_ID,
                new CreateRatingRequest(999L, 3, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("customer of the order");
    }

    @Test
    @DisplayName("createRating – OrderItem không tồn tại → ResourceNotFoundException")
    void createRating_orderItemNotFound() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(completedOrder));
        when(orderItemRepository.findById(new OrderItemId(ORDER_ID, ITEM_ID)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.createRating(ORDER_ID, ITEM_ID,
                new CreateRatingRequest(CUST_ID, 4, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("OrderItem not found");
    }

    @Test
    @DisplayName("createRating – đã đánh giá rồi → ConflictException")
    void createRating_alreadyRated() {
        FoodRating existingRating = FoodRating.builder()
                .ratingId(1L).orderItem(orderItem).ratingScore(4).build();
        orderItem.setRating(existingRating);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(completedOrder));
        when(orderItemRepository.findById(new OrderItemId(ORDER_ID, ITEM_ID)))
                .thenReturn(Optional.of(orderItem));

        assertThatThrownBy(() -> ratingService.createRating(ORDER_ID, ITEM_ID,
                new CreateRatingRequest(CUST_ID, 5, "Ngon")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already been rated");
    }
}
