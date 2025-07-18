package com.unifiz.ecommerce.discount.model;

/**
 * Enumeration of different discount types supported by the system.
 */
public enum DiscountType {
    /**
     * Brand-specific discounts (e.g., "40% off on PUMA")
     */
    BRAND,
    
    /**
     * Category-specific discounts (e.g., "10% off on T-shirts")
     */
    CATEGORY,
    
    /**
     * Bank card offers (e.g., "10% off with ICICI cards")
     */
    BANK_OFFER,
    
    /**
     * Voucher/coupon codes (e.g., "SUPER69 for 69% off")
     */
    VOUCHER,
    
    /**
     * Customer tier-based discounts
     */
    CUSTOMER_TIER
} 