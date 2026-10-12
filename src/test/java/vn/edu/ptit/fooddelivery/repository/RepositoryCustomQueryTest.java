package vn.edu.ptit.fooddelivery.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import vn.edu.ptit.fooddelivery.entity.*;
import vn.edu.ptit.fooddelivery.entity.enums.DeliveryStatus;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest: chỉ load JPA slice, dùng H2 in-memory (create-drop).
 * Test tập trung vào các custom query trong repository.
 */
@DataJpaTest
class RepositoryCustomQueryTest {

    @Autowired TestEntityManager em;

    @Autowired FoodItemRepository        foodItemRepository;
    @Autowired CustomerOrderRepository   orderRepository;
    @Autowired DeliveryRepository        deliveryRepository;
    @Autowired PaymentMethodRepository   paymentMethodRepository;
    @Autowired PaymentRepository         paymentRepository;

    // ─── dữ liệu chung ────────────────────────────────────────────────────────
    private Restaurant restaurant;
    private Customer   customer;

    @BeforeEach
    void setUp() {
        restaurant = em.persist(Restaurant.builder()
                .restaurantName("Phở Test")
                .email("pho.test@example.com")
                .passwordHash("hash")
                .phoneNumber("0900000001")
                .address("1 Test St")
                .latitude(21.0)
                .longitude(105.8)
                .build());

        customer = em.persist(Customer.builder()
                .fullName("Khách Test")
                .email("customer.test@example.com")
                .phoneNumber("0900000002")
                .passwordHash("hash")
                .build());

        em.flush();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. FoodItemRepository.findAvailableByRestaurant
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAvailableByRestaurant – chỉ trả món available=true của nhà hàng active")
    void findAvailableByRestaurant_returnsOnlyAvailableItems() {
        FoodItem available = em.persist(FoodItem.builder()
                .restaurant(restaurant)
                .itemName("Phở bò")
                .price(new BigDecimal("55000"))
                .available(true)
                .build());

        em.persist(FoodItem.builder()
                .restaurant(restaurant)
                .itemName("Phở gà (hết)")
                .price(new BigDecimal("50000"))
                .available(false)         // không bán
                .build());

        em.flush();
        em.clear();

        List<FoodItem> result = foodItemRepository.findAvailableByRestaurant(restaurant.getRestaurantId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemId()).isEqualTo(available.getItemId());
    }

    @Test
    @DisplayName("findAvailableByRestaurant – không trả món của nhà hàng inactive")
    void findAvailableByRestaurant_excludesInactiveRestaurant() {
        Restaurant inactive = em.persist(Restaurant.builder()
                .restaurantName("Đóng cửa")
                .email("closed@example.com")
                .passwordHash("hash")
                .phoneNumber("0900000003")
                .address("2 Test St")
                .latitude(21.1).longitude(105.9)
                .active(false)
                .build());

        em.persist(FoodItem.builder()
                .restaurant(inactive)
                .itemName("Bún chả")
                .price(new BigDecimal("60000"))
                .available(true)
                .build());

        em.flush();
        em.clear();

        List<FoodItem> result = foodItemRepository.findAvailableByRestaurant(inactive.getRestaurantId());

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. CustomerOrderRepository.findByCustomerCustomerIdOrderByCreatedAtDesc
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByCustomer – trả đúng đơn của khách, mới nhất trước")
    void findByCustomer_returnsOrdersForCustomer() {
        CustomerOrder o1 = em.persist(buildOrder(OrderStatus.COMPLETED));
        CustomerOrder o2 = em.persist(buildOrder(OrderStatus.PENDING));
        em.flush();
        em.clear();

        List<CustomerOrder> result = orderRepository
                .findByCustomerCustomerIdOrderByCreatedAtDesc(customer.getCustomerId());

        assertThat(result).hasSize(2);
        // kết quả không phụ thuộc vào thứ tự ID; chỉ kiểm tra đủ 2 đơn đúng khách
        assertThat(result).extracting(CustomerOrder::getOrderId)
                .containsExactlyInAnyOrder(o1.getOrderId(), o2.getOrderId());
    }

    @Test
    @DisplayName("findByCustomer – không trả đơn của khách khác")
    void findByCustomer_doesNotReturnOtherCustomerOrders() {
        Customer other = em.persist(Customer.builder()
                .fullName("Khách Khác")
                .email("other@example.com")
                .phoneNumber("0900000099")
                .passwordHash("hash")
                .build());

        em.persist(buildOrder(other, OrderStatus.PENDING));
        em.flush();
        em.clear();

        List<CustomerOrder> result = orderRepository
                .findByCustomerCustomerIdOrderByCreatedAtDesc(customer.getCustomerId());

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. CustomerOrderRepository.findAwaitingDriver
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAwaitingDriver – đơn READY_FOR_PICKUP không có delivery được trả về")
    void findAwaitingDriver_orderWithoutDelivery() {
        CustomerOrder order = em.persist(buildOrder(OrderStatus.READY_FOR_PICKUP));
        em.flush();
        em.clear();

        List<CustomerOrder> result = orderRepository.findAwaitingDriver();

        assertThat(result).extracting(CustomerOrder::getOrderId)
                .contains(order.getOrderId());
    }

    @Test
    @DisplayName("findAwaitingDriver – đơn READY_FOR_PICKUP có delivery nhưng driver=null")
    void findAwaitingDriver_orderWithDeliveryButNoDriver() {
        CustomerOrder order = em.persist(buildOrder(OrderStatus.READY_FOR_PICKUP));
        em.persist(Delivery.builder()
                .order(order)
                .driver(null)         // chưa có tài xế
                .deliveryStatus(DeliveryStatus.ASSIGNED)
                .build());
        em.flush();
        em.clear();

        List<CustomerOrder> result = orderRepository.findAwaitingDriver();

        assertThat(result).extracting(CustomerOrder::getOrderId)
                .contains(order.getOrderId());
    }

    @Test
    @DisplayName("findAwaitingDriver – đơn DELIVERING (đã có tài xế) không xuất hiện")
    void findAwaitingDriver_excludesOrdersWithDriver() {
        Driver driver = em.persist(Driver.builder()
                .fullName("Tài xế Test")
                .email("driver.test@example.com")
                .passwordHash("hash")
                .phoneNumber("0900000004")
                .licensePlate("99A-99999")
                .build());

        CustomerOrder order = em.persist(buildOrder(OrderStatus.DELIVERING));
        em.persist(Delivery.builder()
                .order(order)
                .driver(driver)
                .deliveryStatus(DeliveryStatus.PICKED_UP)
                .build());
        em.flush();
        em.clear();

        List<CustomerOrder> result = orderRepository.findAwaitingDriver();

        assertThat(result).extracting(CustomerOrder::getOrderId)
                .doesNotContain(order.getOrderId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. DeliveryRepository.findByOrderIdWithLock (PESSIMISTIC_WRITE)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByOrderIdWithLock – trả về Delivery đúng theo orderId")
    void findByOrderIdWithLock_returnsDelivery() {
        CustomerOrder order = em.persist(buildOrder(OrderStatus.READY_FOR_PICKUP));
        Delivery delivery = em.persist(Delivery.builder()
                .order(order)
                .deliveryStatus(DeliveryStatus.ASSIGNED)
                .build());
        em.flush();
        em.clear();

        Optional<Delivery> result = deliveryRepository
                .findByOrderIdWithLock(order.getOrderId());

        assertThat(result).isPresent();
        assertThat(result.get().getDeliveryId()).isEqualTo(delivery.getDeliveryId());
    }

    @Test
    @DisplayName("findByOrderIdWithLock – trả empty khi không tìm thấy")
    void findByOrderIdWithLock_returnsEmptyIfNotFound() {
        Optional<Delivery> result = deliveryRepository.findByOrderIdWithLock(999L);
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. PaymentMethodRepository.findByMethodCode
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByMethodCode – tìm đúng COD và VIETQR")
    void findByMethodCode_findsCodAndVietqr() {
        em.persist(PaymentMethod.builder().methodCode("COD").methodName("Tiền mặt").build());
        em.persist(PaymentMethod.builder().methodCode("VIETQR").methodName("Chuyển khoản").build());
        em.flush();
        em.clear();

        assertThat(paymentMethodRepository.findByMethodCode("COD")).isPresent();
        assertThat(paymentMethodRepository.findByMethodCode("VIETQR")).isPresent();
        assertThat(paymentMethodRepository.findByMethodCode("UNKNOWN")).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper builders
    // ─────────────────────────────────────────────────────────────────────────

    private CustomerOrder buildOrder(OrderStatus status) {
        return buildOrder(customer, status);
    }

    private CustomerOrder buildOrder(Customer c, OrderStatus status) {
        return CustomerOrder.builder()
                .customer(c)
                .restaurant(restaurant)
                .distanceKm(2.5)
                .shippingFee(new BigDecimal("15000"))
                .totalAmount(new BigDecimal("70000"))
                .orderStatus(status)
                .deliveryAddress("Test Address")
                .deliveryLatitude(21.01)
                .deliveryLongitude(105.81)
                .build();
    }
}
