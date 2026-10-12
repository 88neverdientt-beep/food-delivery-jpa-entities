package vn.edu.ptit.fooddelivery.dto.response;

import java.math.BigDecimal;

/** Xem trước phí ship và tổng tiền trước khi đặt đơn. */
public record OrderPreviewResponse(
        Integer restaurantDistrictNo,
        Integer deliveryDistrictNo,
        Integer gap,
        BigDecimal shippingFee,
        BigDecimal itemsTotal,
        BigDecimal totalAmount
) {}
