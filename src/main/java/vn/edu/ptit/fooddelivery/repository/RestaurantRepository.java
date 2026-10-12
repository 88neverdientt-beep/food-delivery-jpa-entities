package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.fooddelivery.entity.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByEmail(String email);

    List<Restaurant> findAllByActiveTrue();

    Page<Restaurant> findAllByActiveTrue(Pageable pageable);

    Page<Restaurant> findAllByActiveTrueAndDistrictNo(Integer districtNo, Pageable pageable);
}
