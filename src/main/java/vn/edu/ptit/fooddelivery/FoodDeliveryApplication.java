package vn.edu.ptit.fooddelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import vn.edu.ptit.fooddelivery.config.ShippingProperties;

@SpringBootApplication
@EnableConfigurationProperties(ShippingProperties.class)
public class FoodDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodDeliveryApplication.class, args);
    }
}
