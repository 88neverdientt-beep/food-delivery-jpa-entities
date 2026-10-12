package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.fooddelivery.entity.FoodRating;

public interface FoodRatingRepository extends JpaRepository<FoodRating, Long> {
}
