package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.ptit.fooddelivery.entity.FoodItem;

import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    /**
     * Lấy tất cả món đang bán của một nhà hàng:
     * - Món phải is_available = true
     * - Nhà hàng phải is_active = true
     * Dùng khi khách chọn món để đặt đơn (quy tắc nghiệp vụ 5.1).
     */
    @Query("""
            SELECT f FROM FoodItem f
            WHERE f.restaurant.restaurantId = :restaurantId
              AND f.available = true
              AND f.restaurant.active = true
            ORDER BY f.categoryName, f.itemName
            """)
    List<FoodItem> findAvailableByRestaurant(@Param("restaurantId") Long restaurantId);
}
