package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.FoodRating;

import java.time.LocalDateTime;

public record RatingResponse(
        Long ratingId,
        Long orderId,
        Long itemId,
        String itemName,
        int ratingScore,
        String comment,
        LocalDateTime createdAt
) {
    public static RatingResponse from(FoodRating r) {
        return new RatingResponse(
                r.getRatingId(),
                r.getOrderItem().getOrder().getOrderId(),
                r.getOrderItem().getFoodItem().getItemId(),
                r.getOrderItem().getFoodItem().getItemName(),
                r.getRatingScore(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
