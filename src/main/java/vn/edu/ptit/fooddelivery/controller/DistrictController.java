package vn.edu.ptit.fooddelivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.fooddelivery.config.ShippingProperties;
import vn.edu.ptit.fooddelivery.dto.response.DistrictResponse;

import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final ShippingProperties shippingProperties;

    /**
     * Danh sách quận lấy từ cấu hình hệ thống (1..totalDistricts).
     */
    @GetMapping
    public List<DistrictResponse> getDistricts() {
        return IntStream.rangeClosed(1, shippingProperties.totalDistricts())
                .mapToObj(i -> new DistrictResponse(i, "Quận " + i))
                .toList();
    }
}
