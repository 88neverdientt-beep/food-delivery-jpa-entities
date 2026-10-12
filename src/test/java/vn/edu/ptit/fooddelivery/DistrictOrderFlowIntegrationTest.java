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
import vn.edu.ptit.fooddelivery.dto.request.CreateOrderRequest;
import vn.edu.ptit.fooddelivery.dto.request.OrderItemRequest;
import vn.edu.ptit.fooddelivery.dto.request.RegisterRestaurantRequest;
import vn.edu.ptit.fooddelivery.entity.Customer;
import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.FoodItem;
import vn.edu.ptit.fooddelivery.entity.Restaurant;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.CustomerRepository;
import vn.edu.ptit.fooddelivery.repository.FoodItemRepository;
import vn.edu.ptit.fooddelivery.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class DistrictOrderFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired RestaurantRepository restaurantRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired FoodItemRepository foodItemRepository;
    @Autowired CustomerOrderRepository orderRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(Customer.builder()
                .fullName("Khách Hàng Test")
                .email("customer.district@example.com")
                .passwordHash("hash")
                .phoneNumber("0987654321")
                .address("Số 1 Đại Cồ Việt")
                .build());
    }

    @Test
    @DisplayName("GET /api/districts trả về đủ 10 quận từ cấu hình")
    void getDistricts_returns10Districts() throws Exception {
        mockMvc.perform(get("/api/districts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].districtNo").value(1))
                .andExpect(jsonPath("$[0].districtName").value("Quận 1"))
                .andExpect(jsonPath("$[9].districtNo").value(10))
                .andExpect(jsonPath("$[9].districtName").value("Quận 10"));
    }

    @Test
    @DisplayName("Test 1: Đặt đơn khác quận (quán Q1, giao Q3 -> gap 2 -> ship 30.000). Preview và create khớp nhau")
    void differentDistrictOrder_shippingFeeMatchesPreviewAndDatabase() throws Exception {
        Restaurant restaurantQ1 = restaurantRepository.save(Restaurant.builder()
                .restaurantName("Quán Ăn Quận 1")
                .email("quan1@test.com")
                .passwordHash("hash")
                .phoneNumber("0901000001")
                .address("100 Lê Lợi, Q1")
                .districtNo(1)
                .active(true)
                .build());

        FoodItem item = foodItemRepository.save(FoodItem.builder()
                .restaurant(restaurantQ1)
                .itemName("Phở Bò Đặc Biệt")
                .price(new BigDecimal("50000.00"))
                .categoryName("Món chính")
                .available(true)
                .build());

        // 1. Gọi preview: restaurant Q1, delivery Q3 -> gap 2 -> fee = 2 * 15000 = 30000
        CreateOrderRequest previewReq = new CreateOrderRequest(
                customer.getCustomerId(),
                restaurantQ1.getRestaurantId(),
                "100 Lê Lợi",
                3, // deliveryDistrictNo
                List.of(new OrderItemRequest(item.getItemId(), 2)) // 2 * 50k = 100k
        );

        MvcResult previewResult = mockMvc.perform(post("/api/orders/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(previewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantDistrictNo").value(1))
                .andExpect(jsonPath("$.deliveryDistrictNo").value(3))
                .andExpect(jsonPath("$.gap").value(2))
                .andExpect(jsonPath("$.shippingFee").value(30000))
                .andExpect(jsonPath("$.itemsTotal").value(100000))
                .andExpect(jsonPath("$.totalAmount").value(130000))
                .andReturn();

        // 2. Gọi create order
        CreateOrderRequest createReq = new CreateOrderRequest(
                customer.getCustomerId(),
                restaurantQ1.getRestaurantId(),
                "Số 20 Cách Mạng Tháng 8, Quận 3",
                3, // deliveryDistrictNo
                List.of(new OrderItemRequest(item.getItemId(), 2))
        );

        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantDistrictNo").value(1))
                .andExpect(jsonPath("$.deliveryDistrictNo").value(3))
                .andExpect(jsonPath("$.shippingFee").value(30000))
                .andExpect(jsonPath("$.totalAmount").value(130000))
                .andReturn();

        Long createdOrderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("orderId").asLong();
        CustomerOrder savedOrder = orderRepository.findById(createdOrderId).orElseThrow();
        assertThat(savedOrder.getDeliveryDistrictNo()).isEqualTo(3);
        assertThat(savedOrder.getShippingFee()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("130000.00"));
    }

    @Test
    @DisplayName("Test 2: Đặt đơn cùng quận (quán Q5, giao Q5 -> gap 0 -> ship 15.000). Total = itemsTotal + ship_fee")
    void sameDistrictOrder_feeIsSameDistrictFee_andTotalMatches() throws Exception {
        Restaurant restaurantQ5 = restaurantRepository.save(Restaurant.builder()
                .restaurantName("Cơm Tấm Quận 5")
                .email("quan5@test.com")
                .passwordHash("hash")
                .phoneNumber("0905000005")
                .address("50 Nguyễn Tri Phương, Q5")
                .districtNo(5)
                .active(true)
                .build());

        FoodItem item = foodItemRepository.save(FoodItem.builder()
                .restaurant(restaurantQ5)
                .itemName("Cơm Tấm Sườn Bì")
                .price(new BigDecimal("45000.00"))
                .categoryName("Cơm")
                .available(true)
                .build());

        CreateOrderRequest createReq = new CreateOrderRequest(
                customer.getCustomerId(),
                restaurantQ5.getRestaurantId(),
                "99 Trần Phú, Quận 5",
                5, // Cùng Q5
                List.of(new OrderItemRequest(item.getItemId(), 1))
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantDistrictNo").value(5))
                .andExpect(jsonPath("$.deliveryDistrictNo").value(5))
                .andExpect(jsonPath("$.shippingFee").value(15000))
                .andExpect(jsonPath("$.totalAmount").value(60000)) // 45000 + 15000
                .andReturn();

        Long createdOrderId = objectMapper.readTree(result.getResponse().getContentAsString()).get("orderId").asLong();
        CustomerOrder savedOrder = orderRepository.findById(createdOrderId).orElseThrow();
        assertThat(savedOrder.getDeliveryDistrictNo()).isEqualTo(5);
        assertThat(savedOrder.getShippingFee()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("60000.00"));
    }

    @Test
    @DisplayName("Test 3: Đăng ký nhà hàng mới kèm quận -> query danh sách theo quận đó thấy ngay")
    void registerRestaurant_withDistrict_showsImmediatelyInFilteredList() throws Exception {
        RegisterRestaurantRequest req = new RegisterRestaurantRequest(
                "Lẩu Nấm Quận 7",
                "launam.q7@example.com",
                "Password123!",
                "0907000007",
                "77 Nguyễn Thị Thập, Q7",
                7, // districtNo = 7
                null,
                null
        );

        mockMvc.perform(post("/api/auth/restaurant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lẩu Nấm Quận 7"))
                .andExpect(jsonPath("$.role").value("RESTAURANT"));

        // Query danh sách nhà hàng lọc theo district=7
        mockMvc.perform(get("/api/restaurants?district=7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].restaurantName").value("Lẩu Nấm Quận 7"))
                .andExpect(jsonPath("$.content[0].districtNo").value(7));

        // Query danh sách nhà hàng lọc theo district=8 -> không có quán này
        mockMvc.perform(get("/api/restaurants?district=8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Test 4: Tài xế xem danh sách đơn chờ -> thấy đủ quận nhà hàng, quận giao, phí ship; lọc đúng theo quận")
    void driverAvailableOrders_showsDistrictsAndShippingFee_andFiltersCorrectly() throws Exception {
        Restaurant restaurantQ2 = restaurantRepository.save(Restaurant.builder()
                .restaurantName("Bún Chả Quận 2")
                .email("quan2@test.com")
                .passwordHash("hash")
                .phoneNumber("0902000002")
                .address("22 Thảo Điền, Q2")
                .districtNo(2)
                .active(true)
                .build());

        Restaurant restaurantQ8 = restaurantRepository.save(Restaurant.builder()
                .restaurantName("Hủ Tiếu Quận 8")
                .email("quan8@test.com")
                .passwordHash("hash")
                .phoneNumber("0908000008")
                .address("88 Phạm Thế Hiển, Q8")
                .districtNo(8)
                .active(true)
                .build());

        // Đơn 1: Nhà hàng Q2, Giao Q4 (READY_FOR_PICKUP) -> gap 2 -> ship 30000
        CustomerOrder order1 = orderRepository.save(CustomerOrder.builder()
                .customer(customer)
                .restaurant(restaurantQ2)
                .deliveryDistrictNo(4)
                .deliveryAddress("44 Khánh Hội, Q4")
                .orderStatus(OrderStatus.READY_FOR_PICKUP)
                .shippingFee(new BigDecimal("30000.00"))
                .totalAmount(new BigDecimal("80000.00"))
                .build());

        // Đơn 2: Nhà hàng Q8, Giao Q8 (READY_FOR_PICKUP) -> gap 0 -> ship 15000
        CustomerOrder order2 = orderRepository.save(CustomerOrder.builder()
                .customer(customer)
                .restaurant(restaurantQ8)
                .deliveryDistrictNo(8)
                .deliveryAddress("88 Phạm Thế Hiển, Q8")
                .orderStatus(OrderStatus.READY_FOR_PICKUP)
                .shippingFee(new BigDecimal("15000.00"))
                .totalAmount(new BigDecimal("65000.00"))
                .build());

        // 1. Tài xế xem toàn bộ đơn chờ (không lọc quận)
        mockMvc.perform(get("/api/drivers/deliveries/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].restaurantDistrictNo").isNumber())
                .andExpect(jsonPath("$.content[0].deliveryDistrictNo").isNumber())
                .andExpect(jsonPath("$.content[0].shippingFee").isNumber());

        // 2. Tài xế lọc theo quận 2 (khớp quận nhà hàng của order1)
        mockMvc.perform(get("/api/drivers/deliveries/available?district=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderId").value(order1.getOrderId()))
                .andExpect(jsonPath("$.content[0].restaurantDistrictNo").value(2))
                .andExpect(jsonPath("$.content[0].deliveryDistrictNo").value(4))
                .andExpect(jsonPath("$.content[0].shippingFee").value(30000));

        // 3. Tài xế lọc theo quận 4 (khớp quận giao của order1)
        mockMvc.perform(get("/api/drivers/deliveries/available?district=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderId").value(order1.getOrderId()));

        // 4. Tài xế lọc theo quận 8 (khớp cả nhà hàng và giao của order2)
        mockMvc.perform(get("/api/drivers/deliveries/available?district=8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderId").value(order2.getOrderId()))
                .andExpect(jsonPath("$.content[0].shippingFee").value(15000));

        // 5. Tài xế lọc theo quận 10 (không có đơn nào)
        mockMvc.perform(get("/api/drivers/deliveries/available?district=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
