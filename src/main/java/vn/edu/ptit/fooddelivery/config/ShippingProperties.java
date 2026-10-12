package vn.edu.ptit.fooddelivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Tham số cấu hình giao hàng theo quận đọc từ application.yml (prefix: app.shipping).
 */
@ConfigurationProperties(prefix = "app.shipping")
public record ShippingProperties(
        /** Tổng số quận trong thành phố (1..totalDistricts). */
        int totalDistricts,
        /** Phí ship khi cùng quận (gap = 0). */
        BigDecimal sameDistrictFee,
        /** Phí mỗi đơn vị chênh lệch quận (gap >= 1: gap * perGapFee). */
        BigDecimal perGapFee
) {
    public double baseFee() { return 15000.0; }
    public double perKmFee() { return 5000.0; }
    public double baseKm() { return 2.0; }
    public double maxFee() { return 50000.0; }
}
