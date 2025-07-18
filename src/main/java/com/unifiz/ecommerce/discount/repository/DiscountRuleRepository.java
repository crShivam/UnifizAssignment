package com.unifiz.ecommerce.discount.repository;

import com.unifiz.ecommerce.discount.model.DiscountRule;
import com.unifiz.ecommerce.discount.model.DiscountType;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing discount rules.
 * In a real application, this would be backed by a database.
 */
public interface DiscountRuleRepository {
    
    /**
     * Find all active discount rules
     */
    List<DiscountRule> findAllActive();
    
    /**
     * Find discount rules by type
     */
    List<DiscountRule> findByType(DiscountType type);
    
    /**
     * Find a discount rule by its name/code
     */
    Optional<DiscountRule> findByName(String name);
    
    /**
     * Find brand-specific discount rules for a given brand
     */
    List<DiscountRule> findBrandDiscounts(String brand);
    
    /**
     * Find category-specific discount rules for a given category
     */
    List<DiscountRule> findCategoryDiscounts(String category);
    
    /**
     * Find bank offers for a specific bank
     */
    List<DiscountRule> findBankOffers(String bankName);
    
    /**
     * Save or update a discount rule
     */
    DiscountRule save(DiscountRule discountRule);
    
    /**
     * Delete a discount rule by ID
     */
    void deleteById(String id);
} 