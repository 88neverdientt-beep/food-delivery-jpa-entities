package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.FoodItem;

import java.math.BigDecimal;

public record FoodItemResponse(
        Long itemId,
        String itemName,
        String categoryName,
        BigDecimal price
) {
    public static FoodItemResponse from(FoodItem fi) {
        return new FoodItemResponse(
                fi.getItemId(),
                fi.getItemName(),
                fi.getCategoryName(),
                fi.getPrice()
        );
    }
}
