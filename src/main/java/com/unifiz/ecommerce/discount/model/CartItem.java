package com.unifiz.ecommerce.discount.model;

import lombok.Data;
import lombok.Builder;

/**
 * Represents an item in the shopping cart.
 * Contains product details along with quantity and size information.
 */
@Data
@Builder
public class CartItem {
    /**
     * The product being added to cart
     */
    private Product product;
    
    /**
     * Quantity of the product in the cart
     */
    private int quantity;
    
    /**
     * Size of the product (e.g., "S", "M", "L", "XL", "42", "10")
     */
    private String size;
} 