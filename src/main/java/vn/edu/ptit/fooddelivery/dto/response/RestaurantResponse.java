package vn.edu.ptit.fooddelivery.dto.response;

import vn.edu.ptit.fooddelivery.entity.Restaurant;

public record RestaurantResponse(
        Long restaurantId,
        String restaurantName,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude
) {
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(
                r.getRestaurantId(),
                r.getRestaurantName(),
                r.getAddress(),
                r.getPhoneNumber(),
                r.getLatitude(),
                r.getLongitude()
        );
    }
}
