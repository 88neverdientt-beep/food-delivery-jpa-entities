package vn.edu.ptit.fooddelivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.fooddelivery.dto.request.LoginRequest;
import vn.edu.ptit.fooddelivery.dto.request.RegisterCustomerRequest;
import vn.edu.ptit.fooddelivery.dto.response.AuthResponse;
import vn.edu.ptit.fooddelivery.entity.Customer;
import vn.edu.ptit.fooddelivery.exception.BusinessException;
import vn.edu.ptit.fooddelivery.exception.ConflictException;
import vn.edu.ptit.fooddelivery.exception.ResourceNotFoundException;
import vn.edu.ptit.fooddelivery.repository.CustomerRepository;
import vn.edu.ptit.fooddelivery.repository.DriverRepository;
import vn.edu.ptit.fooddelivery.repository.RestaurantRepository;

import vn.edu.ptit.fooddelivery.dto.request.RegisterDriverRequest;
import vn.edu.ptit.fooddelivery.dto.request.RegisterRestaurantRequest;
import vn.edu.ptit.fooddelivery.entity.Driver;
import vn.edu.ptit.fooddelivery.entity.Restaurant;

/**
 * Auth đơn giản (không JWT thực sự).
 * Password lưu plaintext cho demo – thay bằng BCrypt khi cần production.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository   customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final DriverRepository     driverRepository;
    private final vn.edu.ptit.fooddelivery.util.ShippingFeeCalculator shippingFeeCalculator;

    // ─── Customer ─────────────────────────────────────────────────────────────

    @PostMapping("/customer/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerCustomer(@Valid @RequestBody RegisterCustomerRequest req) {
        if (customerRepository.findByEmail(req.email()).isPresent()) {
            throw new ConflictException("Email đã tồn tại: " + req.email());
        }
        Customer saved = customerRepository.save(Customer.builder()
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(req.password())   // demo: plaintext
                .phoneNumber(req.phoneNumber())
                .address(req.address())
                .build());
        return new AuthResponse(saved.getCustomerId(), saved.getFullName(), saved.getEmail(), "CUSTOMER");
    }

    @PostMapping("/customer/login")
    public AuthResponse loginCustomer(@Valid @RequestBody LoginRequest req) {
        var customer = customerRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        if (!customer.getPasswordHash().equals(req.password())) {
            throw new BusinessException("Sai mật khẩu");
        }
        return new AuthResponse(customer.getCustomerId(), customer.getFullName(), customer.getEmail(), "CUSTOMER");
    }

    // ─── Restaurant ───────────────────────────────────────────────────────────

    @PostMapping("/restaurant/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerRestaurant(@Valid @RequestBody RegisterRestaurantRequest req) {
        if (restaurantRepository.findByEmail(req.email()).isPresent()) {
            throw new ConflictException("Email nhà hàng đã tồn tại: " + req.email());
        }
        shippingFeeCalculator.validateDistrict(req.districtNo(), "Quận nhà hàng");
        Restaurant saved = restaurantRepository.save(Restaurant.builder()
                .restaurantName(req.restaurantName())
                .email(req.email())
                .passwordHash(req.password())   // demo: plaintext
                .phoneNumber(req.phoneNumber())
                .address(req.address())
                .districtNo(req.districtNo())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .active(true)
                .build());
        return new AuthResponse(saved.getRestaurantId(), saved.getRestaurantName(), saved.getEmail(), "RESTAURANT");
    }

    @PostMapping("/restaurant/login")
    public AuthResponse loginRestaurant(@Valid @RequestBody LoginRequest req) {
        var restaurant = restaurantRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nhà hàng"));
        if (!restaurant.getPasswordHash().equals(req.password())) {
            throw new BusinessException("Sai mật khẩu");
        }
        return new AuthResponse(restaurant.getRestaurantId(), restaurant.getRestaurantName(), restaurant.getEmail(), "RESTAURANT");
    }

    // ─── Driver ───────────────────────────────────────────────────────────────

    @PostMapping("/driver/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerDriver(@Valid @RequestBody RegisterDriverRequest req) {
        if (driverRepository.findByEmail(req.email()).isPresent()) {
            throw new ConflictException("Email tài xế đã tồn tại: " + req.email());
        }
        Driver saved = driverRepository.save(Driver.builder()
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(req.password())   // demo: plaintext
                .phoneNumber(req.phoneNumber())
                .licensePlate(req.licensePlate())
                .online(false)
                .busy(false)
                .build());
        return new AuthResponse(saved.getDriverId(), saved.getFullName(), saved.getEmail(), "DRIVER");
    }

    @PostMapping("/driver/login")
    public AuthResponse loginDriver(@Valid @RequestBody LoginRequest req) {
        var driver = driverRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản tài xế"));
        if (!driver.getPasswordHash().equals(req.password())) {
            throw new BusinessException("Sai mật khẩu");
        }
        return new AuthResponse(driver.getDriverId(), driver.getFullName(), driver.getEmail(), "DRIVER");
    }
}
