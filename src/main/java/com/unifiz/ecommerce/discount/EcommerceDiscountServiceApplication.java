package com.unifiz.ecommerce.discount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the E-commerce Discount Service.
 * 
 * This application provides a comprehensive discount calculation system
 * for fashion e-commerce platforms supporting:
 * - Brand-specific discounts
 * - Category-specific discounts  
 * - Bank card offers
 * - Voucher codes
 * 
 * @author Unifiz Assignment
 */
@SpringBootApplication
public class EcommerceDiscountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceDiscountServiceApplication.class, args);
    }
} 