package com.unifiz.ecommerce.discount.config;

import com.unifiz.ecommerce.discount.model.*;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Test data container for demonstration scenarios.
 * Contains sample products, cart items, customers, and payment info for testing.
 */
@Data
@Builder
public class TestData {
    
    private List<Product> products;
    private List<CartItem> cartItems;
    private CustomerProfile customer;
    private PaymentInfo paymentInfo;
    private List<DiscountRule> discountRules;
    
    /**
     * Sample PUMA T-shirt product
     */
    public Product getPumaTeeshirt() {
        return Product.builder()
                .id("PUMA_TSHIRT_001")
                .brand("PUMA")
                .brandTier(BrandTier.PREMIUM)
                .category("T-shirts")
                .basePrice(new BigDecimal("2000.00"))
                .currentPrice(new BigDecimal("2000.00"))
                .build();
    }
    
    /**
     * Sample Nike shoes product
     */
    public Product getNikeShoes() {
        return Product.builder()
                .id("NIKE_SHOES_001")
                .brand("Nike")
                .brandTier(BrandTier.PREMIUM)
                .category("Shoes")
                .basePrice(new BigDecimal("5000.00"))
                .currentPrice(new BigDecimal("5000.00"))
                .build();
    }
    
    /**
     * Sample Adidas T-shirt product
     */
    public Product getAdidasTshirt() {
        return Product.builder()
                .id("ADIDAS_TSHIRT_001")
                .brand("Adidas")
                .brandTier(BrandTier.PREMIUM)
                .category("T-shirts")
                .basePrice(new BigDecimal("1800.00"))
                .currentPrice(new BigDecimal("1800.00"))
                .build();
    }
    
    /**
     * Sample customer profile
     */
    public CustomerProfile getSampleCustomer() {
        return CustomerProfile.builder()
                .id("CUSTOMER_001")
                .tier("GOLD")
                .email("john.doe@example.com")
                .totalPurchaseValue(new BigDecimal("50000.00"))
                .orderCount(15)
                .build();
    }
    
    /**
     * Sample ICICI bank payment info
     */
    public PaymentInfo getIciciPaymentInfo() {
        return PaymentInfo.builder()
                .method("CARD")
                .bankName("ICICI")
                .cardType("CREDIT")
                .build();
    }
    
    /**
     * Sample HDFC bank payment info
     */
    public PaymentInfo getHdfcPaymentInfo() {
        return PaymentInfo.builder()
                .method("CARD")
                .bankName("HDFC")
                .cardType("DEBIT")
                .build();
    }
    
    /**
     * Creates a sample cart with PUMA T-shirt for the test scenario
     */
    public List<CartItem> createTestCart() {
        Product pumaProduct = getPumaTeeshirt();
        
        CartItem cartItem = CartItem.builder()
                .product(pumaProduct)
                .quantity(2)
                .size("L")
                .build();
                
        return List.of(cartItem);
    }
    
    /**
     * Creates a mixed cart with multiple brands and categories
     */
    public List<CartItem> createMixedCart() {
        return List.of(
            CartItem.builder()
                .product(getPumaTeeshirt())
                .quantity(1)
                .size("L")
                .build(),
            CartItem.builder()
                .product(getNikeShoes())
                .quantity(1)
                .size("42")
                .build(),
            CartItem.builder()
                .product(getAdidasTshirt())
                .quantity(2)
                .size("M")
                .build()
        );
    }
} 