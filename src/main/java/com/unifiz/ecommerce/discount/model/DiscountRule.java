package com.unifiz.ecommerce.discount.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a discount rule configuration.
 * Can be used for brand discounts, category discounts, bank offers, and voucher codes.
 */
@Getter
@Setter
@Data
@Builder
public class DiscountRule {
    /**
     * Unique identifier for the discount rule
     */
    private String id;
    
    /**
     * Name/code of the discount (e.g., "PUMA40", "ICICI10", "SUPER69")
     */
    private String name;
    
    /**
     * Type of discount (BRAND, CATEGORY, BANK_OFFER, VOUCHER)
     */
    private DiscountType type;
    
    /**
     * Discount value (percentage or fixed amount)
     */
    private BigDecimal discountValue;
    
    /**
     * Whether discount is percentage-based (true) or fixed amount (false)
     */
    private boolean isPercentage;
    
    /**
     * Applicable brands (for brand-specific discounts)
     */
    private List<String> applicableBrands;
    
    /**
     * Applicable categories (for category-specific discounts)
     */
    private List<String> applicableCategories;
    
    /**
     * Applicable bank names (for bank offers)
     */
    private List<String> applicableBanks;
    
    /**
     * Required customer tiers for eligibility
     */
    private List<String> requiredCustomerTiers;
    
    /**
     * Minimum cart value for discount eligibility
     */
    private BigDecimal minimumCartValue;
    
    /**
     * Maximum discount amount (cap for percentage discounts)
     */
    private BigDecimal maximumDiscountAmount;
    
    /**
     * Start date of the discount validity
     */
    private LocalDateTime validFrom;
    
    /**
     * End date of the discount validity
     */
    private LocalDateTime validTo;
    
    /**
     * Whether the discount is currently active
     */
    private boolean isActive;
    
    /**
     * Priority of the discount (higher values get applied first)
     */
    private int priority;
    
    /**
     * Description of the discount for display purposes
     */
    private String description;
} 