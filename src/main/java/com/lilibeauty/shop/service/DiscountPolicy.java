package com.lilibeauty.shop.service;

/**
 * A rule that reduces the price of an order.
 * An interface (not an abstract class) because discounts share no state or code,
 * only a behaviour: this lets OrderService accept any new discount without changes.
 */
public interface DiscountPolicy {

    /**
     * @param amount the amount the discount applies to, in EUR
     * @return the amount to subtract, in EUR (never negative, never above {@code amount})
     */
    double discountFor(double amount);
}
