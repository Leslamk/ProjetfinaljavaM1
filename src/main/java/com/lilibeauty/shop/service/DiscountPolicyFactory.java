package com.lilibeauty.shop.service;

import com.lilibeauty.shop.exception.InvalidPromoCodeException;
import com.lilibeauty.shop.model.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides which discounts apply to a customer.
 * It is the only place that knows the concrete discount classes, so OrderService
 * only depends on the DiscountPolicy interface (dependency inversion).
 */
public final class DiscountPolicyFactory {

    private DiscountPolicyFactory() {
    }

    /** Loyalty discount only. */
    public static List<DiscountPolicy> loyaltyOnly(Customer customer) {
        List<DiscountPolicy> policies = new ArrayList<>();
        policies.add(new LoyaltyDiscount(customer.getTier()));
        return policies;
    }

    /**
     * Loyalty discount plus the promo code, if one was typed.
     *
     * @param promoCode may be null or blank, meaning "no code"
     * @throws InvalidPromoCodeException if a non-blank code is unknown
     */
    public static List<DiscountPolicy> forCustomer(Customer customer, String promoCode)
            throws InvalidPromoCodeException {
        List<DiscountPolicy> policies = loyaltyOnly(customer);
        if (promoCode != null && !promoCode.isBlank()) {
            policies.add(PromoCodeDiscount.of(promoCode));
        }
        return policies;
    }
}
