package vn.edu.ptit.fooddelivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.fooddelivery.dto.request.CreateRatingRequest;
import vn.edu.ptit.fooddelivery.dto.response.RatingResponse;
import vn.edu.ptit.fooddelivery.entity.CustomerOrder;
import vn.edu.ptit.fooddelivery.entity.FoodRating;
import vn.edu.ptit.fooddelivery.entity.OrderItem;
import vn.edu.ptit.fooddelivery.entity.OrderItemId;
import vn.edu.ptit.fooddelivery.entity.enums.OrderStatus;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ConflictException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerOrderRepository;
import vn.edu.ptit.fooddelivery.repository.FoodRatingRepository;
import vn.edu.ptit.fooddelivery.repository.OrderItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository     orderItemRepository;
    private final FoodRatingRepository    ratingRepository;

    // ─── Commands ─────────────────────────────────────────────────────────────

    /**
     * Tạo đánh giá cho một món trong đơn đã hoàn tất. (Quy tắc 5.8)
     * <ul>
     *   <li>Đơn phải COMPLETED.</li>
     *   <li>Người đánh giá phải là khách của đơn đó.</li>
     *   <li>Mỗi (orderId, itemId) chỉ được đánh giá một lần.</li>
     * </ul>
     */
    @Transactional
    public RatingResponse createRating(Long orderId, Long itemId, CreateRatingRequest req) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(
                    "Ratings are only allowed for COMPLETED orders. Current: " + order.getOrderStatus());
        }
        if (!order.getCustomer().getCustomerId().equals(req.customerId())) {
            throw new BusinessException(
                    "Only the customer of the order can rate items.");
        }

        OrderItem orderItem = orderItemRepository.findById(new OrderItemId(orderId, itemId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OrderItem not found: orderId=" + orderId + ", itemId=" + itemId));

        if (orderItem.getRating() != null) {
            throw new ConflictException(
                    "Item " + itemId + " in order " + orderId + " has already been rated.");
        }

        FoodRating rating = FoodRating.builder()
                .orderItem(orderItem)
                .ratingScore(req.ratingScore())
                .comment(req.comment())
                .build();

        return RatingResponse.from(ratingRepository.save(rating));
    }
}
