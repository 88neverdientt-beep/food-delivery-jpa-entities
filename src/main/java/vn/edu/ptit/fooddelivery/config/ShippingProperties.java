package vn.edu.ptit.fooddelivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tham số phí ship đọc từ application.yml (prefix: app.shipping).
 * <pre>
 *   shipping_fee = max(base_fee, base_fee + (distance_km - base_km) * per_km_fee)
 *   shipping_fee = min(shipping_fee, max_fee)
 * </pre>
 */
@ConfigurationProperties(prefix = "app.shipping")
public record ShippingProperties(
        /** Phí cơ bản (VND). */
        double baseFee,
        /** Phí thêm mỗi km (VND). */
        double perKmFee,
        /** Số km đầu tính bằng base_fee. */
        double baseKm,
        /** Phí tối đa (VND). */
        double maxFee
) {}
