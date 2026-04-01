package com.douyin.shipping;

import com.douyin.shipping.config.ApiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApiConfig.class)
public class ShippingToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShippingToolApplication.class, args);
    }
}
