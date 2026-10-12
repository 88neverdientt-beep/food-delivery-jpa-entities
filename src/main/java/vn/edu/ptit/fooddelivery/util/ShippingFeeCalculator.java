package vn.edu.ptit.fooddelivery.util;

import vn.edu.ptit.fooddelivery.config.ShippingProperties;

import java.math.BigDecimal;

/**
 * Class thuần tính phí giao hàng theo quận.
 * Không phụ thuộc DB hay Spring Web.
 */
public class ShippingFeeCalculator {

    private final int totalDistricts;
    private final BigDecimal sameDistrictFee;
    private final BigDecimal perGapFee;

    public ShippingFeeCalculator(int totalDistricts, BigDecimal sameDistrictFee, BigDecimal perGapFee) {
        if (totalDistricts < 1) {
            throw new IllegalArgumentException("Tổng số quận phải lớn hơn hoặc bằng 1");
        }
        if (sameDistrictFee == null || sameDistrictFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí cùng quận không hợp lệ");
        }
        if (perGapFee == null || perGapFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Phí mỗi khoảng cách quận không hợp lệ");
        }
        this.totalDistricts = totalDistricts;
        this.sameDistrictFee = sameDistrictFee;
        this.perGapFee = perGapFee;
    }

    public ShippingFeeCalculator(ShippingProperties properties) {
        this(properties.totalDistricts(), properties.sameDistrictFee(), properties.perGapFee());
    }

    /**
     * Kiểm tra tính hợp lệ của quận (1..totalDistricts).
     */
    public void validateDistrict(Integer districtNo, String fieldName) {
        if (districtNo == null || districtNo < 1 || districtNo > totalDistricts) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ: " + districtNo
                    + ". Quận hợp lệ phải từ 1 đến " + totalDistricts + ".");
        }
    }

    /**
     * Tính phí giao hàng theo gap giữa 2 quận:
     * gap = |restaurantDistrictNo - deliveryDistrictNo|
     * Nếu gap == 0 -> sameDistrictFee
     * Nếu gap >= 1 -> gap * perGapFee
     */
    public BigDecimal calculateFee(Integer restaurantDistrictNo, Integer deliveryDistrictNo) {
        validateDistrict(restaurantDistrictNo, "Quận nhà hàng");
        validateDistrict(deliveryDistrictNo, "Quận giao");

        int gap = calculateGap(restaurantDistrictNo, deliveryDistrictNo);
        if (gap == 0) {
            return sameDistrictFee;
        }
        return perGapFee.multiply(BigDecimal.valueOf(gap));
    }

    /**
     * Tính khoảng cách gap giữa 2 quận.
     */
    public int calculateGap(Integer restaurantDistrictNo, Integer deliveryDistrictNo) {
        validateDistrict(restaurantDistrictNo, "Quận nhà hàng");
        validateDistrict(deliveryDistrictNo, "Quận giao");
        return Math.abs(restaurantDistrictNo - deliveryDistrictNo);
    }

    public int getTotalDistricts() {
        return totalDistricts;
    }

    public BigDecimal getSameDistrictFee() {
        return sameDistrictFee;
    }

    public BigDecimal getPerGapFee() {
        return perGapFee;
    }
}
