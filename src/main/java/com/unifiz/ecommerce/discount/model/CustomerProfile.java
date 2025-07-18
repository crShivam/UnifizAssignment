package com.unifiz.ecommerce.discount.model;

import lombok.Data;
import lombok.Builder;

/**
 * Represents customer profile information.
 * Used for customer-specific discount eligibility and tier-based offers.
 */
@Data
@Builder
public class CustomerProfile {
    /**
     * Unique identifier for the customer
     */
    private String id;
    
    /**
     * Customer tier (e.g., "GOLD", "SILVER", "BRONZE", "PREMIUM")
     * Used for tier-specific discounts and eligibility
     */
    private String tier;
    
    /**
     * Customer email address
     */
    private String email;
    
    /**
     * Customer's total purchase history value
     * Used for determining loyalty discounts
     */
    private java.math.BigDecimal totalPurchaseValue;
    
    /**
     * Number of orders placed by the customer
     */
    private Integer orderCount;
} 