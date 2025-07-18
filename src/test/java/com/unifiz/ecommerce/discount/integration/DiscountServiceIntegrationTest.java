package com.unifiz.ecommerce.discount.integration;

import com.unifiz.ecommerce.discount.config.TestData;
import com.unifiz.ecommerce.discount.model.*;
import com.unifiz.ecommerce.discount.repository.InMemoryDiscountRuleRepository;
import com.unifiz.ecommerce.discount.service.DiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete discount service flow.
 * Tests the entire system with all components wired together.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Discount Service Integration Tests")
class DiscountServiceIntegrationTest {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private InMemoryDiscountRuleRepository discountRuleRepository;

    @Autowired
    private TestData testData;

    @BeforeEach
    void setUp() {
        // Repository is already populated by TestDataConfig
        // Just verify it has the expected rules
        assertFalse(discountRuleRepository.findAllActive().isEmpty());
    }

    @Test
    @DisplayName("Should handle complete PUMA T-shirt scenario with all discounts")
    void shouldHandleCompletePumaTshirtScenarioWithAllDiscounts() {
        // Arrange - Test scenario from assignment:
        // PUMA T-shirt with "Min 40% off"
        // Additional 10% off on T-shirts category
        // ICICI bank offer of 10% instant discount
        
        List<CartItem> cartItems = testData.createTestCart(); // 2 PUMA T-shirts @ 2000 each = 4000
        CustomerProfile customer = testData.getSampleCustomer();
        PaymentInfo paymentInfo = testData.getIciciPaymentInfo();

        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                cartItems, customer, Optional.of(paymentInfo));

        // Assert
        assertEquals(new BigDecimal("4000.00"), result.getOriginalPrice());
        
        // Verify all expected discounts are applied
        assertTrue(result.getAppliedDiscounts().containsKey("BRAND_PUMA_DISCOUNT"));
        assertTrue(result.getAppliedDiscounts().containsKey("CATEGORY_T-shirts_DISCOUNT"));
        assertTrue(result.getAppliedDiscounts().containsKey("BANK_ICICI_OFFER"));
        
        // Verify discount amounts
        // Brand: 40% of 4000 = 1600
        assertEquals(new BigDecimal("1600.00"), result.getAppliedDiscounts().get("BRAND_PUMA_DISCOUNT"));
        
        // Category: 10% of 4000 = 400  
        assertEquals(new BigDecimal("400.00"), result.getAppliedDiscounts().get("CATEGORY_T-shirts_DISCOUNT"));
        
        // Bank: 10% of (4000-1600-400) = 10% of 2000 = 200
        assertEquals(new BigDecimal("200.00"), result.getAppliedDiscounts().get("BANK_ICICI_OFFER"));
        
        // Final price: 4000 - 1600 - 400 - 200 = 1800
        assertEquals(new BigDecimal("1800.00"), result.getFinalPrice());
        
        // Verify message
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Total savings: ₹2200"));
    }

    @Test
    @DisplayName("Should handle mixed cart with multiple brands and categories")
    void shouldHandleMixedCartWithMultipleBrandsAndCategories() {
        // Arrange
        List<CartItem> mixedCart = testData.createMixedCart();
        // Cart contains: PUMA T-shirt (2000), Nike Shoes (5000), 2x Adidas T-shirts (3600)
        // Total: 10600
        
        CustomerProfile customer = testData.getSampleCustomer();
        PaymentInfo hdfcPayment = testData.getHdfcPaymentInfo();

        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                mixedCart, customer, Optional.of(hdfcPayment));

        // Assert
        assertEquals(new BigDecimal("10600.00"), result.getOriginalPrice());
        
        // Should have multiple discounts applied for different brands/categories
        assertTrue(result.getAppliedDiscounts().size() > 0);
        
        // Final price should be less than original
        assertTrue(result.getFinalPrice().compareTo(result.getOriginalPrice()) < 0);
    }

    @Test
    @DisplayName("Should validate SUPER69 voucher for eligible customer")
    void shouldValidateSuper69VoucherForEligibleCustomer() {
        // Arrange
        List<CartItem> cartItems = testData.createTestCart(); // 4000 total
        CustomerProfile goldCustomer = testData.getSampleCustomer(); // GOLD tier

        // Act
        boolean isValid = discountService.validateDiscountCode("SUPER69", cartItems, goldCustomer);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should apply SUPER69 voucher correctly")
    void shouldApplySuper69VoucherCorrectly() {
        // Arrange
        List<CartItem> cartItems = testData.createTestCart(); // 4000 total
        CustomerProfile customer = testData.getSampleCustomer();

        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                cartItems, customer, "SUPER69", Optional.empty());

        // Assert
        assertTrue(result.getAppliedDiscounts().containsKey("VOUCHER_SUPER69"));
        
        // SUPER69 gives 69% off, but has max discount of 5000
        // After brand (1600) and category (400) discounts: 4000 - 1600 - 400 = 2000
        // 69% of 2000 = 1380
        assertEquals(new BigDecimal("1380.00"), result.getAppliedDiscounts().get("VOUCHER_SUPER69"));
    }

    @Test
    @DisplayName("Should reject invalid voucher codes")
    void shouldRejectInvalidVoucherCodes() {
        // Arrange
        List<CartItem> cartItems = testData.createTestCart();
        CustomerProfile customer = testData.getSampleCustomer();

        // Act & Assert
        assertFalse(discountService.validateDiscountCode("INVALID", cartItems, customer));
        assertFalse(discountService.validateDiscountCode("EXPIRED", cartItems, customer));
        assertFalse(discountService.validateDiscountCode("", cartItems, customer));
    }

    @Test
    @DisplayName("Should handle no discounts scenario gracefully")
    void shouldHandleNoDiscountsScenarioGracefully() {
        // Arrange - Create a cart that doesn't qualify for any discounts
        Product genericProduct = Product.builder()
                .id("GENERIC_001")
                .brand("UnknownBrand")
                .category("UnknownCategory")
                .currentPrice(new BigDecimal("100.00"))
                .build();

        CartItem cartItem = CartItem.builder()
                .product(genericProduct)
                .quantity(1)
                .size("M")
                .build();

        CustomerProfile customer = CustomerProfile.builder()
                .id("CUSTOMER_LOW")
                .tier("BRONZE")
                .build();

        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                List.of(cartItem), customer, Optional.empty());

        // Assert
        assertEquals(new BigDecimal("100.00"), result.getOriginalPrice());
        assertEquals(new BigDecimal("100.00"), result.getFinalPrice());
        assertTrue(result.getAppliedDiscounts().isEmpty());
        assertEquals("No discounts applied.", result.getMessage());
    }

    @Test
    @DisplayName("Should handle maximum discount caps correctly")
    void shouldHandleMaximumDiscountCapsCorrectly() {
        // Arrange - Create expensive PUMA product that would exceed discount cap
        Product expensivePumaProduct = Product.builder()
                .id("PUMA_EXPENSIVE")
                .brand("PUMA")
                .category("T-shirts")
                .currentPrice(new BigDecimal("20000.00"))
                .build();

        CartItem cartItem = CartItem.builder()
                .product(expensivePumaProduct)
                .quantity(1)
                .size("L")
                .build();

        CustomerProfile customer = testData.getSampleCustomer();

        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                List.of(cartItem), customer, Optional.empty());

        // Assert
        // 40% of 20000 = 8000, but PUMA discount has max of 2000
        assertEquals(new BigDecimal("2000.00"), result.getAppliedDiscounts().get("BRAND_PUMA_DISCOUNT"));
        
        // 10% of 20000 = 2000, but T-shirt discount has max of 500  
        assertEquals(new BigDecimal("500.00"), result.getAppliedDiscounts().get("CATEGORY_T-shirts_DISCOUNT"));
        
        // Final: 20000 - 2000 - 500 = 17500
        assertEquals(new BigDecimal("17500.00"), result.getFinalPrice());
    }

    @Test
    @DisplayName("Should handle discount stacking order correctly")
    void shouldHandleDiscountStackingOrderCorrectly() {
        // Arrange
        List<CartItem> cartItems = testData.createTestCart(); // 4000 total
        CustomerProfile customer = testData.getSampleCustomer();
        PaymentInfo paymentInfo = testData.getIciciPaymentInfo();

        // Act - Apply voucher and bank offer together
        DiscountedPrice result = discountService.calculateCartDiscounts(
                cartItems, customer, "SUPER69", Optional.of(paymentInfo));

        // Assert - Verify discount order: brand/category → voucher → bank
        assertTrue(result.getAppliedDiscounts().containsKey("BRAND_PUMA_DISCOUNT"));
        assertTrue(result.getAppliedDiscounts().containsKey("CATEGORY_T-shirts_DISCOUNT"));
        assertTrue(result.getAppliedDiscounts().containsKey("VOUCHER_SUPER69"));
        assertTrue(result.getAppliedDiscounts().containsKey("BANK_ICICI_OFFER"));
        
        // Bank discount should be applied on the amount after all previous discounts
        // Original: 4000
        // After brand/category: 4000 - 1600 - 400 = 2000
        // After voucher: 2000 - 1380 = 620
        // Bank offer: 10% of 620 = 62
        assertEquals(new BigDecimal("62.00"), result.getAppliedDiscounts().get("BANK_ICICI_OFFER"));
    }
} 