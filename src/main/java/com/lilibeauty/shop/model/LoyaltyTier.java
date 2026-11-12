package com.lilibeauty.shop.model;

/** Customer loyalty levels. An enum fits because the set of tiers is fixed and each carries a discount rate. */
public enum LoyaltyTier {
    STANDARD(0.00),
    SILVER(0.05),
    GOLD(0.10);

    private final double discountRate;

    LoyaltyTier(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}
