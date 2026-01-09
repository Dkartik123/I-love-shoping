package com.iloveshopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for I Love Shopping e-commerce platform.
 * 
 * This application provides a full-featured B2C e-commerce platform with:
 * - User authentication (JWT, OAuth2, 2FA)
 * - Product catalog with faceted search
 * - Shopping cart and checkout
 * - Order management
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class ILoveShoppingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ILoveShoppingApplication.class, args);
    }
}
