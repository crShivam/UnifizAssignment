package com.unifiz.ecommerce.discount.service;

import com.unifiz.ecommerce.discount.exception.DiscountCalculationException;
import com.unifiz.ecommerce.discount.exception.DiscountValidationException;
import com.unifiz.ecommerce.discount.model.*;
import com.unifiz.ecommerce.discount.repository.DiscountRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DiscountService for e-commerce discount calculations.
 * Applies discounts in prioritized order: brand/category → voucher → bank offers.
 * Supports multiple discount types with proper validation and error handling.
 * 
 * @author Unifiz Assignment
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    
    private final DiscountRuleRepository discountRuleRepository;
    
    /**
     * Calculates discounts for cart items without voucher code.
     * 
     * @param cartItems list of items in the cart
     * @param customer customer profile for eligibility checks
     * @param paymentInfo optional payment information for bank offers
     * @return calculated discount pricing with breakdown
     * @throws DiscountCalculationException if calculation fails
     */
    @Override
    public DiscountedPrice calculateCartDiscounts(
            List<CartItem> cartItems,
            CustomerProfile customer,
            Optional<PaymentInfo> paymentInfo) throws DiscountCalculationException {
        return calculateCartDiscounts(cartItems, customer, null, paymentInfo);
    }
    
    /**
     * Calculates comprehensive discounts for cart items with optional voucher.
     * Applies discounts in sequence: brand/category, voucher, bank offers.
     * 
     * @param cartItems list of items in the cart
     * @param customer customer profile for tier-based discounts
     * @param discountCode optional voucher code to apply
     * @param paymentInfo optional payment info for bank-specific offers
     * @return complete discount breakdown with final pricing
     * @throws DiscountCalculationException if calculation process fails
     */
    @Override
    public DiscountedPrice calculateCartDiscounts(
            List<CartItem> cartItems,
            CustomerProfile customer,
            String discountCode,
            Optional<PaymentInfo> paymentInfo) throws DiscountCalculationException {
        
        try {
            validateInputs(cartItems, customer);
            
            BigDecimal originalPrice = calculateOriginalPrice(cartItems);
            Map<String, BigDecimal> appliedDiscounts = new LinkedHashMap<>();
            BigDecimal currentPrice = originalPrice;
            
            // Step 1: Apply brand and category discounts
            currentPrice = applyBrandAndCategoryDiscounts(cartItems, currentPrice, appliedDiscounts);
            
            // Step 2: Apply voucher/discount code if provided
            if (discountCode != null && !discountCode.trim().isEmpty()) {
                currentPrice = applyVoucherDiscount(cartItems, customer, discountCode, currentPrice, appliedDiscounts);
            }
            
            // Step 3: Apply bank offers if payment info is available
            if (paymentInfo.isPresent()) {
                currentPrice = applyBankOffers(cartItems, customer, paymentInfo.get(), currentPrice, appliedDiscounts);
            }
            
            String message = buildDiscountMessage(appliedDiscounts, originalPrice, currentPrice);
            
            return DiscountedPrice.builder()
                    .originalPrice(originalPrice)
                    .finalPrice(currentPrice)
                    .appliedDiscounts(appliedDiscounts)
                    .message(message)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error calculating cart discounts", e);
            throw new DiscountCalculationException("Failed to calculate discounts: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates discount code eligibility for given cart and customer.
     * Checks code existence, validity period, customer tier, and cart applicability.
     * 
     * @param code discount code to validate
     * @param cartItems cart items for applicability check
     * @param customer customer profile for eligibility verification
     * @return true if code is valid and applicable
     * @throws DiscountValidationException if validation process fails
     */
    @Override
    public boolean validateDiscountCode(
            String code,
            List<CartItem> cartItems,
            CustomerProfile customer) throws DiscountValidationException {
        
        try {
            if (code == null || code.trim().isEmpty()) {
                return false;
            }
            
            Optional<DiscountRule> ruleOpt = discountRuleRepository.findByName(code);
            if (ruleOpt.isEmpty()) {
                return false;
            }
            
            DiscountRule rule = ruleOpt.get();
            
            // Check if rule is active and within validity period
            if (!isRuleValid(rule)) {
                return false;
            }
            
            // Check customer tier requirements
            if (!isCustomerEligible(rule, customer)) {
                return false;
            }
            
            // Check minimum cart value
            BigDecimal cartValue = calculateOriginalPrice(cartItems);
            if (rule.getMinimumCartValue() != null && cartValue.compareTo(rule.getMinimumCartValue()) < 0) {
                return false;
            }
            
            // Check brand and category restrictions for vouchers
            if (rule.getType() == DiscountType.VOUCHER) {
                return isVoucherApplicableToCart(rule, cartItems);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error validating discount code: " + code, e);
            throw new DiscountValidationException("Failed to validate discount code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates required input parameters for discount calculations.
     * 
     * @param cartItems cart items to validate
     * @param customer customer profile to validate
     * @throws IllegalArgumentException if inputs are invalid
     */
    private void validateInputs(List<CartItem> cartItems, CustomerProfile customer) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart items cannot be null or empty");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer profile cannot be null");
        }
    }
    
    /**
     * Calculates total original price of all cart items.
     * 
     * @param cartItems list of cart items with quantities
     * @return total original price rounded to 2 decimal places
     */
    private BigDecimal calculateOriginalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getCurrentPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Applies brand and category discounts to eligible cart items.
     * Calculates best available discounts for each product's brand and category.
     * 
     * @param cartItems cart items to apply discounts to
     * @param currentPrice current cart price before brand/category discounts
     * @param appliedDiscounts map to track applied discount details
     * @return price after applying brand and category discounts
     */
    private BigDecimal applyBrandAndCategoryDiscounts(
            List<CartItem> cartItems,
            BigDecimal currentPrice,
            Map<String, BigDecimal> appliedDiscounts) {
        
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            BigDecimal itemTotal = product.getCurrentPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            
            // Apply brand discounts
            List<DiscountRule> brandDiscounts = discountRuleRepository.findBrandDiscounts(product.getBrand());
            BigDecimal brandDiscount = calculateBestDiscount(brandDiscounts, itemTotal);
            if (brandDiscount.compareTo(BigDecimal.ZERO) > 0) {
                totalDiscount = totalDiscount.add(brandDiscount);
                String discountKey = "BRAND_" + product.getBrand() + "_DISCOUNT";
                appliedDiscounts.merge(discountKey, brandDiscount, BigDecimal::add);
            }
            
            // Apply category discounts
            List<DiscountRule> categoryDiscounts = discountRuleRepository.findCategoryDiscounts(product.getCategory());
            BigDecimal categoryDiscount = calculateBestDiscount(categoryDiscounts, itemTotal);
            if (categoryDiscount.compareTo(BigDecimal.ZERO) > 0) {
                totalDiscount = totalDiscount.add(categoryDiscount);
                String discountKey = "CATEGORY_" + product.getCategory() + "_DISCOUNT";
                appliedDiscounts.merge(discountKey, categoryDiscount, BigDecimal::add);
            }
        }
        
        return currentPrice.subtract(totalDiscount).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Applies voucher discount if valid and applicable to cart.
     * Validates voucher code and applies discount to current price.
     * 
     * @param cartItems cart items for voucher applicability check
     * @param customer customer profile for eligibility
     * @param discountCode voucher code to apply
     * @param currentPrice current cart price before voucher discount
     * @param appliedDiscounts map to track applied discount details
     * @return price after applying voucher discount
     */
    private BigDecimal applyVoucherDiscount(
            List<CartItem> cartItems,
            CustomerProfile customer,
            String discountCode,
            BigDecimal currentPrice,
            Map<String, BigDecimal> appliedDiscounts) {
        
        Optional<DiscountRule> ruleOpt = discountRuleRepository.findByName(discountCode);
        if (ruleOpt.isEmpty() || !isRuleValid(ruleOpt.get()) || !isCustomerEligible(ruleOpt.get(), customer)) {
            return currentPrice;
        }
        
        DiscountRule rule = ruleOpt.get();
        if (rule.getType() != DiscountType.VOUCHER) {
            return currentPrice;
        }
        
        if (!isVoucherApplicableToCart(rule, cartItems)) {
            return currentPrice;
        }
        
        BigDecimal discount = calculateDiscount(rule, currentPrice);
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            appliedDiscounts.put("VOUCHER_" + discountCode, discount);
            return currentPrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        }
        
        return currentPrice;
    }
    
    /**
     * Applies bank offers based on payment information.
     * Checks for valid bank offers matching payment method.
     * 
     * @param cartItems cart items for context
     * @param customer customer profile for eligibility
     * @param paymentInfo payment information containing bank details
     * @param currentPrice current cart price before bank offers
     * @param appliedDiscounts map to track applied discount details
     * @return price after applying bank offers
     */
    private BigDecimal applyBankOffers(
            List<CartItem> cartItems,
            CustomerProfile customer,
            PaymentInfo paymentInfo,
            BigDecimal currentPrice,
            Map<String, BigDecimal> appliedDiscounts) {
        
        if (paymentInfo.getBankName() == null) {
            return currentPrice;
        }
        
        List<DiscountRule> bankOffers = discountRuleRepository.findBankOffers(paymentInfo.getBankName());
        
        for (DiscountRule offer : bankOffers) {
            if (isRuleValid(offer) && isCustomerEligible(offer, customer)) {
                BigDecimal discount = calculateDiscount(offer, currentPrice);
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    String discountKey = "BANK_" + paymentInfo.getBankName() + "_OFFER";
                    appliedDiscounts.put(discountKey, discount);
                    return currentPrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        
        return currentPrice;
    }
    
    /**
     * Finds the best discount from available rules for given amount.
     * 
     * @param rules list of applicable discount rules
     * @param amount amount to calculate discount on
     * @return highest applicable discount amount
     */
    private BigDecimal calculateBestDiscount(List<DiscountRule> rules, BigDecimal amount) {
        return rules.stream()
                .filter(this::isRuleValid)
                .map(rule -> calculateDiscount(rule, amount))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Calculates discount amount based on rule configuration.
     * Handles both percentage and fixed amount discounts with caps.
     * 
     * @param rule discount rule containing calculation parameters
     * @param amount base amount to calculate discount on
     * @return calculated discount amount
     */
    private BigDecimal calculateDiscount(DiscountRule rule, BigDecimal amount) {
        BigDecimal discount;
        
        if (rule.isPercentage()) {
            discount = amount.multiply(rule.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            // Apply maximum discount cap if specified
            if (rule.getMaximumDiscountAmount() != null && 
                discount.compareTo(rule.getMaximumDiscountAmount()) > 0) {
                discount = rule.getMaximumDiscountAmount();
            }
        } else {
            discount = rule.getDiscountValue();
            
            // Ensure discount doesn't exceed the amount
            if (discount.compareTo(amount) > 0) {
                discount = amount;
            }
        }
        
        return discount.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Validates if discount rule is currently active and within validity period.
     * 
     * @param rule discount rule to validate
     * @return true if rule is valid and active
     */
    private boolean isRuleValid(DiscountRule rule) {
        if (!rule.isActive()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (rule.getValidFrom() != null && now.isBefore(rule.getValidFrom())) {
            return false;
        }
        
        if (rule.getValidTo() != null && now.isAfter(rule.getValidTo())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if customer is eligible for the discount rule based on tier requirements.
     * 
     * @param rule discount rule with tier requirements
     * @param customer customer profile with tier information
     * @return true if customer meets tier requirements
     */
    private boolean isCustomerEligible(DiscountRule rule, CustomerProfile customer) {
        if (rule.getRequiredCustomerTiers() == null || rule.getRequiredCustomerTiers().isEmpty()) {
            return true;
        }
        
        return rule.getRequiredCustomerTiers().contains(customer.getTier());
    }
    
    /**
     * Validates if voucher is applicable to cart items based on brand/category restrictions.
     * 
     * @param rule voucher rule with brand/category restrictions
     * @param cartItems cart items to check against restrictions
     * @return true if voucher applies to at least one cart item
     */
    private boolean isVoucherApplicableToCart(DiscountRule rule, List<CartItem> cartItems) {
        // If no specific brand/category restrictions, voucher applies to entire cart
        if ((rule.getApplicableBrands() == null || rule.getApplicableBrands().isEmpty()) &&
            (rule.getApplicableCategories() == null || rule.getApplicableCategories().isEmpty())) {
            return true;
        }
        
        // Check if any item in cart matches the voucher's brand/category restrictions
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            
            boolean brandMatches = rule.getApplicableBrands() == null || 
                                  rule.getApplicableBrands().isEmpty() || 
                                  rule.getApplicableBrands().contains(product.getBrand());
            
            boolean categoryMatches = rule.getApplicableCategories() == null || 
                                     rule.getApplicableCategories().isEmpty() || 
                                     rule.getApplicableCategories().contains(product.getCategory());
            
            if (brandMatches && categoryMatches) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Builds human-readable discount summary message with total savings.
     * 
     * @param appliedDiscounts map of applied discounts with amounts
     * @param originalPrice original cart price before discounts
     * @param finalPrice final cart price after all discounts
     * @return formatted discount summary message
     */
    private String buildDiscountMessage(Map<String, BigDecimal> appliedDiscounts, 
                                       BigDecimal originalPrice, 
                                       BigDecimal finalPrice) {
        if (appliedDiscounts.isEmpty()) {
            return "No discounts applied.";
        }
        
        BigDecimal totalSavings = originalPrice.subtract(finalPrice);
        StringBuilder message = new StringBuilder();
        message.append("Applied discounts: ");
        
        List<String> discountDescriptions = appliedDiscounts.entrySet().stream()
                .map(entry -> entry.getKey() + " (₹" + entry.getValue() + ")")
                .collect(Collectors.toList());
        
        message.append(String.join(", ", discountDescriptions));
        message.append(". Total savings: ₹").append(totalSavings);
        
        return message.toString();
    }
} 