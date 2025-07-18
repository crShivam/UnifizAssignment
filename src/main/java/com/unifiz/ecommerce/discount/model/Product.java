package com.unifiz.ecommerce.discount.model;

import java.math.BigDecimal;
import lombok.Data;
import lombok.Builder;

/**
 * Represents a product in the e-commerce system.
 * Contains product information including pricing and brand details.
 */
@Data
@Builder
public class Product {
    /**
     * Unique identifier for the product
     */
    private String id;
    
    /**
     * Brand name of the product (e.g., "PUMA", "Nike", "Adidas")
     */
    private String brand;
    
    /**
     * Tier classification of the brand
     */
    private BrandTier brandTier;
    
    /**
     * Product category (e.g., "T-shirts", "Shoes", "Jeans")
     */
    private String category;
    
    /**
     * Original base price of the product before any discounts
     */
    private BigDecimal basePrice;
    
    /**
     * Current price after brand/category discounts but before cart-level discounts
     */
    private BigDecimal currentPrice;
} 