package vn.edu.ptit.fooddelivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.fooddelivery.dto.request.*;
import vn.edu.ptit.fooddelivery.entity.*;
import vn.edu.ptit.fooddelivery.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test – luồng chính:
 * Đặt đơn → Nhà hàng xác nhận/chuẩn bị/sẵn sàng
 *         → Tài xế nhận → Lấy hàng → Giao hàng → Hoàn tất → Đánh giá
 *
 * Dùng @Transactional để rollback sau mỗi test (H2 in-memory).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class OrderFlowIntegrationTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    @Autowired RestaurantRepository    restaurantRepository;
    @Autowired CustomerRepository      customerRepository;
    @Autowired DriverRepository        driverRepository;
    @Autowired FoodItemRepository      foodItemRepository;
    @Autowired PaymentMethodRepository paymentMethodRepository;

    // ─── Shared test data ─────────────────────────────────────────────────────
    private Restaurant   restaurant;
    private Customer     customer;
    private Driver       driver;
    private FoodItem     foodItem;
    private PaymentMethod codMethod;

    @BeforeEach
    void setUp() {
        codMethod = paymentMethodRepository.save(PaymentMethod.builder()
                .methodCode("COD").methodName("Tiền mặt khi nhận hàng").build());
        paymentMethodRepository.save(PaymentMethod.builder()
                .methodCode("VIETQR").methodName("Chuyển khoản VietQR").build());

        restaurant = restaurantRepository.save(Restaurant.builder()
                .restaurantName("Phở Integration Test")
                .email("it.restaurant@example.com")
                .passwordHash("hash")
                .phoneNumber("0900000011")
                .address("1 Test St, Hà Nội")
                .districtNo(1)
                .latitude(21.0285)
                .longitude(105.8542)
                .active(true)
                .build());

        customer = customerRepository.save(Customer.builder()
                .fullName("Khách Integration Test")
                .email("it.customer@example.com")
                .phoneNumber("0900000012")
                .passwordHash("hash")
                .build());

        driver = driverRepository.save(Driver.builder()
                .fullName("Tài Xế Integration Test")
                .email("it.driver@example.com")
                .passwordHash("hash")
                .phoneNumber("0900000013")
                .licensePlate("IT-00001")
                .online(true)
                .busy(false)
                .build());

        foodItem = foodItemRepository.save(FoodItem.builder()
                .restaurant(restaurant)
                .itemName("Phở bò đặc biệt")
                .categoryName("Phở")
                .price(new BigDecimal("65000"))
                .available(true)
                .build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main flow: đặt đơn → xác nhận → chuẩn bị → sẵn sàng → tài xế nhận
    //           → lấy hàng → giao thành công → đánh giá
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Luồng chính: đặt đơn → nhà hàng xác nhận → tài xế nhận → giao → hoàn tất → đánh giá")
    void mainFlow_orderToRating() throws Exception {

        // ── Step 1: Khách đặt đơn ─────────────────────────────────────────────
        CreateOrderRequest createReq = new CreateOrderRequest(
                customer.getCustomerId(),
                restaurant.getRestaurantId(),
                "15 Hàng Buồm, Hoàn Kiếm, Hà Nội",
                1,
                List.of(new OrderItemRequest(foodItem.getItemId(), 2))
        );

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderStatus").value("PENDING"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].itemName").value("Phở bò đặc biệt"))
                .andReturn();

        Long orderId = extractLong(createResult, "orderId");
        assertThat(orderId).isNotNull();

        // ── Step 2: Nhà hàng xác nhận (CONFIRMED) ─────────────────────────────
        patchOrderStatus(orderId, "CONFIRMED");

        // ── Step 3: Nhà hàng bắt đầu chuẩn bị (PREPARING) ────────────────────
        patchOrderStatus(orderId, "PREPARING");

        // ── Step 4: Nhà hàng báo sẵn sàng lấy (READY_FOR_PICKUP) ─────────────
        // → Service tự động tạo Delivery record (driver=null)
        patchOrderStatus(orderId, "READY_FOR_PICKUP");

        // Kiểm tra GET order phản ánh trạng thái mới
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("READY_FOR_PICKUP"));

        // ── Step 5: Tài xế nhận đơn (PESSIMISTIC_WRITE) ───────────────────────
        AcceptDeliveryRequest acceptReq = new AcceptDeliveryRequest(driver.getDriverId());

        MvcResult acceptResult = mockMvc.perform(post("/api/deliveries/{orderId}/accept", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(driver.getDriverId()))
                .andReturn();

        Long deliveryId = extractLong(acceptResult, "deliveryId");
        assertThat(deliveryId).isNotNull();

        // ── Step 6: Tài xế lấy hàng (PICKED_UP) → order → DELIVERING ─────────
        UpdateDeliveryStatusRequest pickedUpReq = new UpdateDeliveryStatusRequest(
                vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus.PICKED_UP);

        mockMvc.perform(patch("/api/deliveries/{id}/status", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickedUpReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryStatus").value("PICKED_UP"));

        // Kiểm tra order đã DELIVERING
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(jsonPath("$.orderStatus").value("DELIVERING"));

        // ── Step 7: Tài xế giao thành công (DELIVERED) → order → COMPLETED ────
        UpdateDeliveryStatusRequest deliveredReq = new UpdateDeliveryStatusRequest(
                vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus.DELIVERED);

        mockMvc.perform(patch("/api/deliveries/{id}/status", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deliveredReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryStatus").value("DELIVERED"))
                .andExpect(jsonPath("$.deliveredTime").isNotEmpty());

        // Kiểm tra order đã COMPLETED
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(jsonPath("$.orderStatus").value("COMPLETED"));

        // ── Step 8: Khách đánh giá món ────────────────────────────────────────
        CreateRatingRequest ratingReq = new CreateRatingRequest(
                customer.getCustomerId(), 5, "Phở cực ngon, giao nhanh!");

        mockMvc.perform(post("/api/order-items/{orderId}/{itemId}/rating",
                        orderId, foodItem.getItemId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ratingScore").value(5))
                .andExpect(jsonPath("$.comment").value("Phở cực ngon, giao nhanh!"))
                .andExpect(jsonPath("$.itemName").value("Phở bò đặc biệt"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Error cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createOrder – nhà hàng không tồn tại → 404")
    void createOrder_restaurantNotFound_404() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest(
                customer.getCustomerId(), 9999L,
                "addr", 1,
                List.of(new OrderItemRequest(foodItem.getItemId(), 1)));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("acceptDelivery lần 2 (cùng đơn) → 409 Conflict")
    void acceptDelivery_secondDriver_409() throws Exception {
        // Tạo đơn và đưa đến READY_FOR_PICKUP
        Long orderId = createOrderAndAdvanceTo("READY_FOR_PICKUP");

        // Tài xế 1 nhận thành công
        AcceptDeliveryRequest req = new AcceptDeliveryRequest(driver.getDriverId());
        mockMvc.perform(post("/api/deliveries/{orderId}/accept", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Tài xế 2 (cùng driver để đơn giản, service check driver != null trước)
        Driver driver2 = driverRepository.save(Driver.builder()
                .fullName("Tài Xế 2").email("driver2.it@example.com")
                .passwordHash("hash").phoneNumber("0900000099")
                .licensePlate("IT-00002").online(true).busy(false).build());

        AcceptDeliveryRequest req2 = new AcceptDeliveryRequest(driver2.getDriverId());
        mockMvc.perform(post("/api/deliveries/{orderId}/accept", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("cancelOrder – đơn PENDING khách hủy → 200, orderStatus=CANCELLED")
    void cancelOrder_byCustomer_success() throws Exception {
        Long orderId = createOrder();

        CancelOrderRequest req = new CancelOrderRequest(customer.getCustomerId(), "CUSTOMER");
        mockMvc.perform(patch("/api/orders/{id}/cancel", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));
    }

    @Test
    @DisplayName("createOrder – @Valid thất bại (items rỗng) → 400")
    void createOrder_emptyItems_400() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest(
                customer.getCustomerId(), restaurant.getRestaurantId(),
                "addr", 1,
                List.of()); // rỗng → vi phạm @NotEmpty

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/restaurants/{id}/items – trả về danh sách món available")
    void getRestaurantItems_returnsAvailableItems() throws Exception {
        mockMvc.perform(get("/api/restaurants/{id}/items", restaurant.getRestaurantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].itemName").value("Phở bò đặc biệt"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────────────────────

    private void patchOrderStatus(Long orderId, String status) throws Exception {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(
                vn.edu.ptit.fooddelivery.entity.enums.OrderStatus.valueOf(status));
        mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                        .param("restaurantId", restaurant.getRestaurantId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(status));
    }

    private Long createOrder() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest(
                customer.getCustomerId(), restaurant.getRestaurantId(),
                "Delivery Address", 1,
                List.of(new OrderItemRequest(foodItem.getItemId(), 1)));

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractLong(result, "orderId");
    }

    private Long createOrderAndAdvanceTo(String targetStatus) throws Exception {
        Long orderId = createOrder();
        String[] steps = switch (targetStatus) {
            case "CONFIRMED"        -> new String[]{"CONFIRMED"};
            case "PREPARING"        -> new String[]{"CONFIRMED", "PREPARING"};
            case "READY_FOR_PICKUP" -> new String[]{"CONFIRMED", "PREPARING", "READY_FOR_PICKUP"};
            default -> new String[]{};
        };
        for (String s : steps) patchOrderStatus(orderId, s);
        return orderId;
    }

    @SuppressWarnings("unchecked")
    private Long extractLong(MvcResult result, String field) throws Exception {
        String json = result.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        return ((Number) map.get(field)).longValue();
    }
}
