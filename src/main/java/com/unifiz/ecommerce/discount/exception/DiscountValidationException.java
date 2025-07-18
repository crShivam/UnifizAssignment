package com.unifiz.ecommerce.discount.exception;

/**
 * Exception thrown when discount code validation fails.
 * This can occur when a discount code is invalid, expired, or not applicable to the current cart.
 */
public class DiscountValidationException extends RuntimeException {
    
    /**
     * Constructs a new DiscountValidationException with the specified detail message.
     *
     * @param message the detail message explaining why validation failed
     */
    public DiscountValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DiscountValidationException with the specified detail message and cause.
     *
     * @param message the detail message explaining why validation failed
     * @param cause the underlying cause of the exception
     */
    public DiscountValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 