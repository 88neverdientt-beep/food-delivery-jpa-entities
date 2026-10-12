package vn.edu.ptit.fooddelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.fooddelivery.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
}
