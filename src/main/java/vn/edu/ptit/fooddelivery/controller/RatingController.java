package vn.edu.ptit.fooddelivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.request.CreateRatingRequest;
import vn.edu.ptit.fooddelivery.dto.response.RatingResponse;
import vn.edu.ptit.fooddelivery.service.RatingService;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * Khách đánh giá một món trong đơn đã hoàn tất.
     * POST /api/order-items/{orderId}/{itemId}/rating
     * Body: { "customerId": 1, "ratingScore": 5, "comment": "Rất ngon!" }
     */
    @PostMapping("/{orderId}/{itemId}/rating")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponse createRating(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody CreateRatingRequest req) {
        return ratingService.createRating(orderId, itemId, req);
    }
}
