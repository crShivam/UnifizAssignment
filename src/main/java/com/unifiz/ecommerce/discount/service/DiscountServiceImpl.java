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
 * Implementation of DiscountService that handles e-commerce discount calculations.
 * Applies discounts in the order: brand/category → voucher → bank offers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    
    private final DiscountRuleRepository discountRuleRepository;
    
    @Override
    public DiscountedPrice calculateCartDiscounts(
            List<CartItem> cartItems,
            CustomerProfile customer,
            Optional<PaymentInfo> paymentInfo) throws DiscountCalculationException {
        return calculateCartDiscounts(cartItems, customer, null, paymentInfo);
    }
    
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
    
    private void validateInputs(List<CartItem> cartItems, CustomerProfile customer) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart items cannot be null or empty");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer profile cannot be null");
        }
    }
    
    private BigDecimal calculateOriginalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getCurrentPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
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
    
    private BigDecimal calculateBestDiscount(List<DiscountRule> rules, BigDecimal amount) {
        return rules.stream()
                .filter(this::isRuleValid)
                .map(rule -> calculateDiscount(rule, amount))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    
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
    
    private boolean isCustomerEligible(DiscountRule rule, CustomerProfile customer) {
        if (rule.getRequiredCustomerTiers() == null || rule.getRequiredCustomerTiers().isEmpty()) {
            return true;
        }
        
        return rule.getRequiredCustomerTiers().contains(customer.getTier());
    }
    
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