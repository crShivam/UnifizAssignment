package com.unifiz.ecommerce.discount.model;

import lombok.Data;
import lombok.Builder;

/**
 * Contains payment information for the transaction.
 * Used to determine applicable bank offers and payment method discounts.
 */
@Data
@Builder
public class PaymentInfo {
    /**
     * Payment method used (e.g., "CARD", "UPI", "NET_BANKING", "WALLET")
     */
    private String method;
    
    /**
     * Bank name for card/net banking payments (e.g., "ICICI", "HDFC", "SBI")
     * Optional field - may be null for non-bank payment methods
     */
    private String bankName;
    
    /**
     * Type of card being used (e.g., "CREDIT", "DEBIT")
     * Optional field - may be null for non-card payment methods
     */
    private String cardType;
} 