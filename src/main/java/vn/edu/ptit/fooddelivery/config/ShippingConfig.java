package vn.edu.ptit.fooddelivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.edu.ptit.fooddelivery.util.ShippingFeeCalculator;

@Configuration
public class ShippingConfig {

    @Bean
    public ShippingFeeCalculator shippingFeeCalculator(ShippingProperties shippingProperties) {
        return new ShippingFeeCalculator(shippingProperties);
    }
}
