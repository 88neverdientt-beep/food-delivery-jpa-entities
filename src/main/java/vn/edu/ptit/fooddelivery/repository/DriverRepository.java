package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.fooddelivery.entity.Driver;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByEmail(String email);
}
