package com.douyin.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShippingToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShippingToolApplication.class, args);
    }
}
