package com.unifiz.ecommerce.discount.exception;

/**
 * Exception thrown when discount calculation fails.
 * This can occur due to invalid cart data, calculation errors, or system failures.
 */
public class DiscountCalculationException extends RuntimeException {
    
    /**
     * Constructs a new DiscountCalculationException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public DiscountCalculationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DiscountCalculationException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause the underlying cause of the exception
     */
    public DiscountCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
} 