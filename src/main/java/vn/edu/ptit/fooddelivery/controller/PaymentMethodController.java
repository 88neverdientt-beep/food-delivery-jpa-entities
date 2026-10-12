package vn.edu.ptit.fooddelivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.fooddelivery.dto.response.PaymentMethodResponse;
import vn.edu.ptit.fooddelivery.repository.PaymentMethodRepository;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodRepository paymentMethodRepository;

    @GetMapping
    public List<PaymentMethodResponse> getAll() {
        return paymentMethodRepository.findAll().stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }
}
