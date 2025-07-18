package com.unifiz.ecommerce.discount.service;

import com.unifiz.ecommerce.discount.exception.DiscountCalculationException;
import com.unifiz.ecommerce.discount.exception.DiscountValidationException;
import com.unifiz.ecommerce.discount.model.*;
import com.unifiz.ecommerce.discount.repository.DiscountRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for DiscountServiceImpl.
 * Tests all discount scenarios, edge cases, and error conditions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiscountService Tests")
class DiscountServiceImplTest {

    @Mock
    private DiscountRuleRepository discountRuleRepository;

    @InjectMocks
    private DiscountServiceImpl discountService;

    private Product pumaProduct;
    private Product nikeProduct;
    private CustomerProfile goldCustomer;
    private CustomerProfile silverCustomer;
    private PaymentInfo iciciPayment;
    private DiscountRule pumaBrandDiscount;
    private DiscountRule tshirtCategoryDiscount;
    private DiscountRule iciciBankOffer;
    private DiscountRule super69Voucher;

    @BeforeEach
    void setUp() {
        // Setup test products
        pumaProduct = Product.builder()
                .id("PUMA_001")
                .brand("PUMA")
                .brandTier(BrandTier.PREMIUM)
                .category("T-shirts")
                .basePrice(new BigDecimal("2000.00"))
                .currentPrice(new BigDecimal("2000.00"))
                .build();

        nikeProduct = Product.builder()
                .id("NIKE_001")
                .brand("Nike")
                .brandTier(BrandTier.PREMIUM)
                .category("Shoes")
                .basePrice(new BigDecimal("5000.00"))
                .currentPrice(new BigDecimal("5000.00"))
                .build();

        // Setup customers
        goldCustomer = CustomerProfile.builder()
                .id("CUSTOMER_001")
                .tier("GOLD")
                .email("gold@example.com")
                .totalPurchaseValue(new BigDecimal("50000"))
                .orderCount(10)
                .build();

        silverCustomer = CustomerProfile.builder()
                .id("CUSTOMER_002")
                .tier("SILVER")
                .email("silver@example.com")
                .totalPurchaseValue(new BigDecimal("20000"))
                .orderCount(5)
                .build();

        // Setup payment info
        iciciPayment = PaymentInfo.builder()
                .method("CARD")
                .bankName("ICICI")
                .cardType("CREDIT")
                .build();

        // Setup discount rules
        pumaBrandDiscount = DiscountRule.builder()
                .id("1")
                .name("PUMA40")
                .type(DiscountType.BRAND)
                .discountValue(new BigDecimal("40"))
                .isPercentage(true)
                .applicableBrands(List.of("PUMA"))
                .minimumCartValue(new BigDecimal("1000"))
                .maximumDiscountAmount(new BigDecimal("2000"))
                .isActive(true)
                .validFrom(LocalDateTime.now().minusDays(30))
                .validTo(LocalDateTime.now().plusDays(30))
                .build();

        tshirtCategoryDiscount = DiscountRule.builder()
                .id("2")
                .name("TSHIRT10")
                .type(DiscountType.CATEGORY)
                .discountValue(new BigDecimal("10"))
                .isPercentage(true)
                .applicableCategories(List.of("T-shirts"))
                .minimumCartValue(new BigDecimal("500"))
                .maximumDiscountAmount(new BigDecimal("500"))
                .isActive(true)
                .validFrom(LocalDateTime.now().minusDays(15))
                .validTo(LocalDateTime.now().plusDays(45))
                .build();

        iciciBankOffer = DiscountRule.builder()
                .id("3")
                .name("ICICI10")
                .type(DiscountType.BANK_OFFER)
                .discountValue(new BigDecimal("10"))
                .isPercentage(true)
                .applicableBanks(List.of("ICICI"))
                .minimumCartValue(new BigDecimal("1000"))
                .maximumDiscountAmount(new BigDecimal("1000"))
                .isActive(true)
                .validFrom(LocalDateTime.now().minusDays(7))
                .validTo(LocalDateTime.now().plusDays(60))
                .build();

        super69Voucher = DiscountRule.builder()
                .id("4")
                .name("SUPER69")
                .type(DiscountType.VOUCHER)
                .discountValue(new BigDecimal("69"))
                .isPercentage(true)
                .minimumCartValue(new BigDecimal("2000"))
                .maximumDiscountAmount(new BigDecimal("5000"))
                .requiredCustomerTiers(List.of("GOLD", "PLATINUM"))
                .isActive(true)
                .validFrom(LocalDateTime.now().minusDays(5))
                .validTo(LocalDateTime.now().plusDays(10))
                .build();
    }

    @Nested
    @DisplayName("Basic Discount Calculations")
    class BasicDiscountCalculations {

        @Test
        @DisplayName("Should calculate brand discount correctly")
        void shouldCalculateBrandDiscountCorrectly() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(1)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

                    when(discountRuleRepository.findBrandDiscounts("PUMA"))
                .thenReturn(List.of(pumaBrandDiscount));
        when(discountRuleRepository.findCategoryDiscounts("T-shirts"))
                .thenReturn(List.of());

            // Act
            DiscountedPrice result = discountService.calculateCartDiscounts(
                    cartItems, goldCustomer, Optional.empty());

            // Assert
            assertEquals(new BigDecimal("2000.00"), result.getOriginalPrice());
            assertEquals(new BigDecimal("1200.00"), result.getFinalPrice()); // 40% off = 800 discount
            assertTrue(result.getAppliedDiscounts().containsKey("BRAND_PUMA_DISCOUNT"));
            assertEquals(new BigDecimal("800.00"), result.getAppliedDiscounts().get("BRAND_PUMA_DISCOUNT"));
        }

        @Test
        @DisplayName("Should calculate category discount correctly")
        void shouldCalculateCategoryDiscountCorrectly() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(1)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findBrandDiscounts("PUMA"))
                    .thenReturn(List.of());
            when(discountRuleRepository.findCategoryDiscounts("T-shirts"))
                    .thenReturn(List.of(tshirtCategoryDiscount));

            // Act
            DiscountedPrice result = discountService.calculateCartDiscounts(
                    cartItems, goldCustomer, Optional.empty());

            // Assert
            assertEquals(new BigDecimal("2000.00"), result.getOriginalPrice());
            assertEquals(new BigDecimal("1800.00"), result.getFinalPrice()); // 10% off = 200 discount
            assertTrue(result.getAppliedDiscounts().containsKey("CATEGORY_T-shirts_DISCOUNT"));
            assertEquals(new BigDecimal("200.00"), result.getAppliedDiscounts().get("CATEGORY_T-shirts_DISCOUNT"));
        }

        @Test
        @DisplayName("Should apply multiple discounts in correct order")
        void shouldApplyMultipleDiscountsInCorrectOrder() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(2) // Total: 4000
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findBrandDiscounts("PUMA"))
                    .thenReturn(List.of(pumaBrandDiscount));
            when(discountRuleRepository.findCategoryDiscounts("T-shirts"))
                    .thenReturn(List.of(tshirtCategoryDiscount));
            when(discountRuleRepository.findBankOffers("ICICI"))
                    .thenReturn(List.of(iciciBankOffer));
            when(discountRuleRepository.findByName("SUPER69"))
                    .thenReturn(Optional.of(super69Voucher));

            // Act
            DiscountedPrice result = discountService.calculateCartDiscounts(
                    cartItems, goldCustomer, "SUPER69", Optional.of(iciciPayment));

            // Assert
            assertEquals(new BigDecimal("4000.00"), result.getOriginalPrice());
            
            // Check all discounts are applied
            assertTrue(result.getAppliedDiscounts().containsKey("BRAND_PUMA_DISCOUNT"));
            assertTrue(result.getAppliedDiscounts().containsKey("CATEGORY_T-shirts_DISCOUNT"));
            assertTrue(result.getAppliedDiscounts().containsKey("VOUCHER_SUPER69"));
            assertTrue(result.getAppliedDiscounts().containsKey("BANK_ICICI_OFFER"));
        }
    }

    @Nested
    @DisplayName("Voucher Validation Tests")
    class VoucherValidationTests {

        @Test
        @DisplayName("Should validate correct voucher code")
        void shouldValidateCorrectVoucherCode() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(2)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findByName("SUPER69"))
                    .thenReturn(Optional.of(super69Voucher));

            // Act
            boolean isValid = discountService.validateDiscountCode("SUPER69", cartItems, goldCustomer);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should reject invalid voucher code")
        void shouldRejectInvalidVoucherCode() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(1)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findByName("INVALID"))
                    .thenReturn(Optional.empty());

            // Act
            boolean isValid = discountService.validateDiscountCode("INVALID", cartItems, goldCustomer);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject voucher for ineligible customer tier")
        void shouldRejectVoucherForIneligibleCustomerTier() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(2)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findByName("SUPER69"))
                    .thenReturn(Optional.of(super69Voucher));

            // Act
            boolean isValid = discountService.validateDiscountCode("SUPER69", cartItems, silverCustomer);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should reject voucher when cart value below minimum")
        void shouldRejectVoucherWhenCartValueBelowMinimum() {
            // Arrange
            Product cheapProduct = Product.builder()
                    .id("CHEAP_001")
                    .brand("Brand")
                    .category("Category")
                    .currentPrice(new BigDecimal("500.00"))
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(cheapProduct)
                    .quantity(1)
                    .size("M")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findByName("SUPER69"))
                    .thenReturn(Optional.of(super69Voucher));

            // Act
            boolean isValid = discountService.validateDiscountCode("SUPER69", cartItems, goldCustomer);

            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Bank Offer Tests")
    class BankOfferTests {

        @Test
        @DisplayName("Should apply bank offer when payment info matches")
        void shouldApplyBankOfferWhenPaymentInfoMatches() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(2) // Total: 4000
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findBrandDiscounts(any()))
                    .thenReturn(List.of());
            when(discountRuleRepository.findCategoryDiscounts(any()))
                    .thenReturn(List.of());
            when(discountRuleRepository.findBankOffers("ICICI"))
                    .thenReturn(List.of(iciciBankOffer));

            // Act
            DiscountedPrice result = discountService.calculateCartDiscounts(
                    cartItems, goldCustomer, Optional.of(iciciPayment));

            // Assert
            assertTrue(result.getAppliedDiscounts().containsKey("BANK_ICICI_OFFER"));
            assertEquals(new BigDecimal("400.00"), result.getAppliedDiscounts().get("BANK_ICICI_OFFER"));
        }

        @Test
        @DisplayName("Should not apply bank offer when payment info missing")
        void shouldNotApplyBankOfferWhenPaymentInfoMissing() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(2)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findBrandDiscounts(any()))
                    .thenReturn(List.of());
            when(discountRuleRepository.findCategoryDiscounts(any()))
                    .thenReturn(List.of());

            // Act
            DiscountedPrice result = discountService.calculateCartDiscounts(
                    cartItems, goldCustomer, Optional.empty());

            // Assert
            assertFalse(result.getAppliedDiscounts().containsKey("BANK_ICICI_OFFER"));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should throw exception for null cart items")
        void shouldThrowExceptionForNullCartItems() {
            // Act & Assert
            assertThrows(DiscountCalculationException.class, () ->
                    discountService.calculateCartDiscounts(null, goldCustomer, Optional.empty()));
        }

        @Test
        @DisplayName("Should throw exception for empty cart items")
        void shouldThrowExceptionForEmptyCartItems() {
            // Act & Assert
            assertThrows(DiscountCalculationException.class, () ->
                    discountService.calculateCartDiscounts(List.of(), goldCustomer, Optional.empty()));
        }

        @Test
        @DisplayName("Should throw exception for null customer")
        void shouldThrowExceptionForNullCustomer() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(1)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            // Act & Assert
            assertThrows(DiscountCalculationException.class, () ->
                    discountService.calculateCartDiscounts(cartItems, null, Optional.empty()));
        }

        @Test
        @DisplayName("Should handle maximum discount cap correctly")
        void shouldHandleMaximumDiscountCapCorrectly() {
            // Arrange
            Product expensiveProduct = Product.builder()
                    .id("EXPENSIVE_001")
                    .brand("PUMA")
                    .category("T-shirts")
                    .currentPrice(new BigDecimal("10000.00"))
                    .build();

            CartItem cartItem = CartItem.builder()
                    .product(expensiveProduct)
                    .quantity(1)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findBrandDiscounts("PUMA"))
                    .thenReturn(List.of(pumaBrandDiscount));
            when(discountRuleRepository.findCategoryDiscounts(any()))
                    .thenReturn(List.of());

            // Act
            DiscountedPrice result = discountService.calculateCartDiscounts(
                    cartItems, goldCustomer, Optional.empty());

            // Assert
            // 40% of 10000 = 4000, but max discount is 2000
            assertEquals(new BigDecimal("2000.00"), result.getAppliedDiscounts().get("BRAND_PUMA_DISCOUNT"));
            assertEquals(new BigDecimal("8000.00"), result.getFinalPrice());
        }

        @Test
        @DisplayName("Should return false for empty or null discount code validation")
        void shouldReturnFalseForEmptyOrNullDiscountCodeValidation() {
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(1)
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            // Act & Assert
            assertFalse(discountService.validateDiscountCode("", cartItems, goldCustomer));
            assertFalse(discountService.validateDiscountCode("   ", cartItems, goldCustomer));
            assertFalse(discountService.validateDiscountCode(null, cartItems, goldCustomer));
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle the assignment test scenario correctly")
        void shouldHandleAssignmentTestScenarioCorrectly() {
            // Test scenario: PUMA T-shirt with 40% brand discount + 10% category discount + 10% ICICI bank offer
            // Arrange
            CartItem cartItem = CartItem.builder()
                    .product(pumaProduct)
                    .quantity(2) // Total: 4000
                    .size("L")
                    .build();
            List<CartItem> cartItems = List.of(cartItem);

            when(discountRuleRepository.findBrandDiscounts("PUMA"))
                    .thenReturn(List.of(pumaBrandDiscount));
            when(discountRuleRepository.findCategoryDiscounts("T-shirts"))
                    .thenReturn(List.of(tshirtCategoryDiscount));
            when(discountRuleRepository.findBankOffers("ICICI"))
                    .thenReturn(List.of(iciciBankOffer));

            // Act
            DiscountedPrice result = discountService.calculateCartDiscounts(
                    cartItems, goldCustomer, Optional.of(iciciPayment));

            // Assert
            assertEquals(new BigDecimal("4000.00"), result.getOriginalPrice());
            
            // Brand discount: 40% of 4000 = 1600
            assertEquals(new BigDecimal("1600.00"), result.getAppliedDiscounts().get("BRAND_PUMA_DISCOUNT"));
            
            // Category discount: 10% of 4000 = 400
            assertEquals(new BigDecimal("400.00"), result.getAppliedDiscounts().get("CATEGORY_T-shirts_DISCOUNT"));
            
            // After brand and category discounts: 4000 - 1600 - 400 = 2000
            // Bank offer: 10% of 2000 = 200
            assertEquals(new BigDecimal("200.00"), result.getAppliedDiscounts().get("BANK_ICICI_OFFER"));
            
            // Final price: 2000 - 200 = 1800
            assertEquals(new BigDecimal("1800.00"), result.getFinalPrice());
            
            // Verify message contains all discounts
            String message = result.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("BRAND_PUMA_DISCOUNT"));
            assertTrue(message.contains("CATEGORY_T-shirts_DISCOUNT"));
            assertTrue(message.contains("BANK_ICICI_OFFER"));
        }
    }
} 