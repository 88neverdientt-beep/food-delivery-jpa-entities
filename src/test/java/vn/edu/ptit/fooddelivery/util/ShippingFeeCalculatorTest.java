package vn.edu.ptit.fooddelivery.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.ptit.fooddelivery.config.ShippingProperties;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingFeeCalculatorTest {

    private ShippingFeeCalculator calculator;

    @BeforeEach
    void setUp() {
        ShippingProperties properties = new ShippingProperties(
                10,
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(15000)
        );
        calculator = new ShippingFeeCalculator(properties);
    }

    @Test
    @DisplayName("gap = 0: cùng quận thì phí ship là phí cùng quận (15,000 VND)")
    void calculateFee_gap0_returnsSameDistrictFee() {
        BigDecimal fee1 = calculator.calculateFee(1, 1);
        BigDecimal fee5 = calculator.calculateFee(5, 5);
        BigDecimal fee10 = calculator.calculateFee(10, 10);

        assertThat(fee1).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(fee5).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(fee10).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("gap = 1: quận kế bên phí = 1 x 15,000 = 15,000 VND")
    void calculateFee_gap1_returns15000() {
        BigDecimal fee = calculator.calculateFee(1, 2);
        assertThat(fee).isEqualByComparingTo(BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("gap = 2: cách 2 quận phí = 2 x 15,000 = 30,000 VND")
    void calculateFee_gap2_returns30000() {
        BigDecimal fee = calculator.calculateFee(1, 3);
        assertThat(fee).isEqualByComparingTo(BigDecimal.valueOf(30000));
    }

    @Test
    @DisplayName("gap = 3: cách 3 quận phí = 3 x 15,000 = 45,000 VND")
    void calculateFee_gap3_returns45000() {
        BigDecimal fee = calculator.calculateFee(2, 5);
        assertThat(fee).isEqualByComparingTo(BigDecimal.valueOf(45000));
    }

    @Test
    @DisplayName("gap = 9: cách xa nhất (quận 1 đến quận 10) phí = 9 x 15,000 = 135,000 VND")
    void calculateFee_gap9_returns135000() {
        BigDecimal fee = calculator.calculateFee(1, 10);
        assertThat(fee).isEqualByComparingTo(BigDecimal.valueOf(135000));
    }

    @Test
    @DisplayName("Tính đối xứng: phí ship quận 2 -> 5 bằng phí ship quận 5 -> 2")
    void calculateFee_isSymmetric() {
        BigDecimal fee2to5 = calculator.calculateFee(2, 5);
        BigDecimal fee5to2 = calculator.calculateFee(5, 2);

        assertThat(fee2to5).isEqualByComparingTo(fee5to2);
        assertThat(fee2to5).isEqualByComparingTo(BigDecimal.valueOf(45000));
    }

    @Test
    @DisplayName("Quận 0 bị từ chối với lỗi rõ ràng")
    void calculateFee_district0_rejected() {
        assertThatThrownBy(() -> calculator.calculateFee(0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quận nhà hàng")
                .hasMessageContaining("1 đến 10");

        assertThatThrownBy(() -> calculator.calculateFee(3, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quận giao")
                .hasMessageContaining("1 đến 10");
    }

    @Test
    @DisplayName("Quận 11 bị từ chối với lỗi rõ ràng")
    void calculateFee_district11_rejected() {
        assertThatThrownBy(() -> calculator.calculateFee(11, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quận nhà hàng")
                .hasMessageContaining("1 đến 10");

        assertThatThrownBy(() -> calculator.calculateFee(4, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quận giao")
                .hasMessageContaining("1 đến 10");
    }

    @Test
    @DisplayName("Quận null bị từ chối với lỗi rõ ràng")
    void calculateFee_districtNull_rejected() {
        assertThatThrownBy(() -> calculator.calculateFee(null, 2))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> calculator.calculateFee(2, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
