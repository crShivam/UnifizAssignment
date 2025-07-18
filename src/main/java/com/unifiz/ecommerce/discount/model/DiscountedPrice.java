package com.unifiz.ecommerce.discount.model;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.Builder;

/**
 * Represents the result of discount calculations for a cart.
 * Contains original price, final price, and breakdown of applied discounts.
 */
@Data
@Builder
public class DiscountedPrice {
    /**
     * Total original price before any discounts
     */
    private BigDecimal originalPrice;
    
    /**
     * Final price after applying all discounts
     */
    private BigDecimal finalPrice;
    
    /**
     * Map of discount names to discount amounts applied
     * Key: discount name (e.g., "PUMA_BRAND_DISCOUNT", "ICICI_BANK_OFFER")
     * Value: discount amount
     */
    private Map<String, BigDecimal> appliedDiscounts;
    
    /**
     * Human-readable message describing the discounts applied
     */
    private String message;
} 