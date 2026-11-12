package com.lilibeauty.shop.service;

import com.lilibeauty.shop.model.LoyaltyTier;

/** Percentage discount that depends on the customer's loyalty tier. */
public class LoyaltyDiscount implements DiscountPolicy {

    private final LoyaltyTier tier;

    public LoyaltyDiscount(LoyaltyTier tier) {
        if (tier == null) {
            throw new IllegalArgumentException("Loyalty tier must not be null");
        }
        this.tier = tier;
    }

    @Override
    public double discountFor(double amount) {
        return Math.round(amount * tier.getDiscountRate() * 100.0) / 100.0;
    }
}
