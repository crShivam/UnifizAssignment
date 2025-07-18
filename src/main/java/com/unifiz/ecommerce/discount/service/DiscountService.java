package com.unifiz.ecommerce.discount.service;

import com.unifiz.ecommerce.discount.model.CartItem;
import com.unifiz.ecommerce.discount.model.CustomerProfile;
import com.unifiz.ecommerce.discount.model.DiscountedPrice;
import com.unifiz.ecommerce.discount.model.PaymentInfo;
import com.unifiz.ecommerce.discount.exception.DiscountCalculationException;
import com.unifiz.ecommerce.discount.exception.DiscountValidationException;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service interface for handling e-commerce discount calculations and validations.
 * Supports multiple types of discounts including brand, category, bank offers, and voucher codes.
 */
@Service
public interface DiscountService {
    
    /**
     * Calculate final price after applying discount logic in the following order:
     * 1. First apply brand/category discounts
     * 2. Then apply coupon codes
     * 3. Then apply bank offers
     *
     * @param cartItems List of items in the cart
     * @param customer Customer profile information
     * @param paymentInfo Optional payment information for bank offers
     * @return Calculated discounted price details with breakdown
     * @throws DiscountCalculationException if calculation fails
     */
    DiscountedPrice calculateCartDiscounts(
        List<CartItem> cartItems,
        CustomerProfile customer,
        Optional<PaymentInfo> paymentInfo
    ) throws DiscountCalculationException;

    /**
     * Calculate cart discounts with a specific discount code.
     *
     * @param cartItems List of items in the cart
     * @param customer Customer profile information
     * @param discountCode Discount code to apply
     * @param paymentInfo Optional payment information for bank offers
     * @return Calculated discounted price details with breakdown
     * @throws DiscountCalculationException if calculation fails
     */
    DiscountedPrice calculateCartDiscounts(
        List<CartItem> cartItems,
        CustomerProfile customer,
        String discountCode,
        Optional<PaymentInfo> paymentInfo
    ) throws DiscountCalculationException;

    /**
     * Validate if a discount code can be applied.
     * Handle specific cases like:
     * - Brand exclusions
     * - Category restrictions
     * - Customer tier requirements
     * - Minimum cart value requirements
     * - Expiry dates
     *
     * @param code Discount code to validate
     * @param cartItems Current cart items
     * @param customer Customer profile
     * @return true if code is valid, false otherwise
     * @throws DiscountValidationException if validation fails
     */
    boolean validateDiscountCode(
        String code,
        List<CartItem> cartItems,
        CustomerProfile customer
    ) throws DiscountValidationException;
} 