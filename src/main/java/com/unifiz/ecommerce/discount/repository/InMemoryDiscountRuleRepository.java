package com.unifiz.ecommerce.discount.repository;

import com.unifiz.ecommerce.discount.model.DiscountRule;
import com.unifiz.ecommerce.discount.model.DiscountType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of DiscountRuleRepository for demonstration purposes.
 * In a production environment, this would be replaced with a database-backed implementation.
 */
@Repository
public class InMemoryDiscountRuleRepository implements DiscountRuleRepository {
    
    private final Map<String, DiscountRule> discountRules = new ConcurrentHashMap<>();
    
    @Override
    public List<DiscountRule> findAllActive() {
        LocalDateTime now = LocalDateTime.now();
        return discountRules.values().stream()
                .filter(rule -> rule.isActive() && 
                               (rule.getValidFrom() == null || rule.getValidFrom().isBefore(now)) &&
                               (rule.getValidTo() == null || rule.getValidTo().isAfter(now)))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscountRule> findByType(DiscountType type) {
        return findAllActive().stream()
                .filter(rule -> rule.getType() == type)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<DiscountRule> findByName(String name) {
        return findAllActive().stream()
                .filter(rule -> rule.getName().equalsIgnoreCase(name))
                .findFirst();
    }
    
    @Override
    public List<DiscountRule> findBrandDiscounts(String brand) {
        return findByType(DiscountType.BRAND).stream()
                .filter(rule -> rule.getApplicableBrands() != null && 
                               rule.getApplicableBrands().contains(brand))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscountRule> findCategoryDiscounts(String category) {
        return findByType(DiscountType.CATEGORY).stream()
                .filter(rule -> rule.getApplicableCategories() != null && 
                               rule.getApplicableCategories().contains(category))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DiscountRule> findBankOffers(String bankName) {
        return findByType(DiscountType.BANK_OFFER).stream()
                .filter(rule -> rule.getApplicableBanks() != null && 
                               rule.getApplicableBanks().contains(bankName))
                .collect(Collectors.toList());
    }
    
    @Override
    public DiscountRule save(DiscountRule discountRule) {
        if (discountRule.getId() == null) {
            discountRule.setId(UUID.randomUUID().toString());
        }
        discountRules.put(discountRule.getId(), discountRule);
        return discountRule;
    }
    
    @Override
    public void deleteById(String id) {
        discountRules.remove(id);
    }
    
    /**
     * Get all discount rules (for testing purposes)
     */
    public Map<String, DiscountRule> getAllRules() {
        return new HashMap<>(discountRules);
    }
    
    /**
     * Clear all discount rules (for testing purposes)
     */
    public void clear() {
        discountRules.clear();
    }
} 