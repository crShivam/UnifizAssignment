package com.unifiz.ecommerce.discount.config;

import com.unifiz.ecommerce.discount.model.DiscountRule;
import com.unifiz.ecommerce.discount.model.DiscountType;
import com.unifiz.ecommerce.discount.repository.DiscountRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Configuration class for setting up test data.
 * Creates the specific scenario mentioned in the assignment:
 * - PUMA T-shirt with "Min 40% off"
 * - Additional 10% off on T-shirts category
 * - ICICI bank offer of 10% instant discount
 */
@Configuration
@RequiredArgsConstructor
public class TestDataConfig {
    
    private final DiscountRuleRepository discountRuleRepository;
    
    @Bean
    public TestData testData() {
        return TestData.builder().build();
    }
    
    /**
     * CommandLineRunner to initialize discount rules on application startup
     */
    @Bean
    public CommandLineRunner initializeDiscountRules() {
        return args -> {
            // Clear any existing rules
            if (discountRuleRepository instanceof com.unifiz.ecommerce.discount.repository.InMemoryDiscountRuleRepository) {
                ((com.unifiz.ecommerce.discount.repository.InMemoryDiscountRuleRepository) discountRuleRepository).clear();
            }
            
            // Create PUMA brand discount - Min 40% off
            DiscountRule pumaBrandDiscount = DiscountRule.builder()
                    .name("PUMA40")
                    .type(DiscountType.BRAND)
                    .discountValue(new BigDecimal("40"))
                    .isPercentage(true)
                    .applicableBrands(List.of("PUMA"))
                    .minimumCartValue(new BigDecimal("1000"))
                    .maximumDiscountAmount(new BigDecimal("2000"))
                    .validFrom(LocalDateTime.now().minusDays(30))
                    .validTo(LocalDateTime.now().plusDays(30))
                    .isActive(true)
                    .priority(10)
                    .description("Minimum 40% off on PUMA products")
                    .build();
            
            // Create T-shirts category discount - Extra 10% off
            DiscountRule tshirtCategoryDiscount = DiscountRule.builder()
                    .name("TSHIRT10")
                    .type(DiscountType.CATEGORY)
                    .discountValue(new BigDecimal("10"))
                    .isPercentage(true)
                    .applicableCategories(List.of("T-shirts"))
                    .minimumCartValue(new BigDecimal("500"))
                    .maximumDiscountAmount(new BigDecimal("500"))
                    .validFrom(LocalDateTime.now().minusDays(15))
                    .validTo(LocalDateTime.now().plusDays(45))
                    .isActive(true)
                    .priority(5)
                    .description("Extra 10% off on T-shirts")
                    .build();
            
            // Create ICICI bank offer - 10% instant discount
            DiscountRule iciciBankOffer = DiscountRule.builder()
                    .name("ICICI10")
                    .type(DiscountType.BANK_OFFER)
                    .discountValue(new BigDecimal("10"))
                    .isPercentage(true)
                    .applicableBanks(List.of("ICICI"))
                    .minimumCartValue(new BigDecimal("1000"))
                    .maximumDiscountAmount(new BigDecimal("1000"))
                    .validFrom(LocalDateTime.now().minusDays(7))
                    .validTo(LocalDateTime.now().plusDays(60))
                    .isActive(true)
                    .priority(1)
                    .description("10% instant discount on ICICI Bank cards")
                    .build();
            
            // Create SUPER69 voucher - 69% off
            DiscountRule super69Voucher = DiscountRule.builder()
                    .name("SUPER69")
                    .type(DiscountType.VOUCHER)
                    .discountValue(new BigDecimal("69"))
                    .isPercentage(true)
                    .minimumCartValue(new BigDecimal("2000"))
                    .maximumDiscountAmount(new BigDecimal("5000"))
                    .requiredCustomerTiers(List.of("GOLD", "PLATINUM"))
                    .validFrom(LocalDateTime.now().minusDays(5))
                    .validTo(LocalDateTime.now().plusDays(10))
                    .isActive(true)
                    .priority(15)
                    .description("SUPER69 - 69% off for Gold and Platinum customers")
                    .build();
            
            // Create additional discount rules for more comprehensive testing
            
            // Nike brand discount
            DiscountRule nikeBrandDiscount = DiscountRule.builder()
                    .name("NIKE30")
                    .type(DiscountType.BRAND)
                    .discountValue(new BigDecimal("30"))
                    .isPercentage(true)
                    .applicableBrands(List.of("Nike"))
                    .minimumCartValue(new BigDecimal("2000"))
                    .maximumDiscountAmount(new BigDecimal("1500"))
                    .validFrom(LocalDateTime.now().minusDays(20))
                    .validTo(LocalDateTime.now().plusDays(20))
                    .isActive(true)
                    .priority(8)
                    .description("30% off on Nike products")
                    .build();
            
            // HDFC bank offer
            DiscountRule hdfcBankOffer = DiscountRule.builder()
                    .name("HDFC15")
                    .type(DiscountType.BANK_OFFER)
                    .discountValue(new BigDecimal("15"))
                    .isPercentage(true)
                    .applicableBanks(List.of("HDFC"))
                    .minimumCartValue(new BigDecimal("3000"))
                    .maximumDiscountAmount(new BigDecimal("800"))
                    .validFrom(LocalDateTime.now().minusDays(10))
                    .validTo(LocalDateTime.now().plusDays(30))
                    .isActive(true)
                    .priority(2)
                    .description("15% instant discount on HDFC Bank cards")
                    .build();
            
            // Shoes category discount
            DiscountRule shoesCategoryDiscount = DiscountRule.builder()
                    .name("SHOES15")
                    .type(DiscountType.CATEGORY)
                    .discountValue(new BigDecimal("15"))
                    .isPercentage(true)
                    .applicableCategories(List.of("Shoes"))
                    .minimumCartValue(new BigDecimal("2000"))
                    .maximumDiscountAmount(new BigDecimal("1000"))
                    .validFrom(LocalDateTime.now().minusDays(5))
                    .validTo(LocalDateTime.now().plusDays(25))
                    .isActive(true)
                    .priority(6)
                    .description("15% off on Shoes")
                    .build();
            
            // Save all discount rules
            discountRuleRepository.save(pumaBrandDiscount);
            discountRuleRepository.save(tshirtCategoryDiscount);
            discountRuleRepository.save(iciciBankOffer);
            discountRuleRepository.save(super69Voucher);
            discountRuleRepository.save(nikeBrandDiscount);
            discountRuleRepository.save(hdfcBankOffer);
            discountRuleRepository.save(shoesCategoryDiscount);
            
            System.out.println("✅ Test discount rules initialized successfully!");
            System.out.println("📋 Available discount scenarios:");
            System.out.println("   - PUMA40: 40% off on PUMA products");
            System.out.println("   - TSHIRT10: 10% off on T-shirts");
            System.out.println("   - ICICI10: 10% instant discount with ICICI cards");
            System.out.println("   - SUPER69: 69% off voucher for Gold/Platinum customers");
            System.out.println("   - NIKE30: 30% off on Nike products");
            System.out.println("   - HDFC15: 15% instant discount with HDFC cards");
            System.out.println("   - SHOES15: 15% off on Shoes");
        };
    }
} 