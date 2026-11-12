package com.lilibeauty.shop.service;

import com.lilibeauty.shop.exception.InvalidPromoCodeException;

import java.util.Map;

/** Percentage discount unlocked by a promo code typed by the customer. */
public class PromoCodeDiscount implements DiscountPolicy {

    // Codes are hard-coded here for simplicity; a real shop would load them from a file or database.
    private static final Map<String, Double> KNOWN_CODES = Map.of(
            "WELCOME10", 0.10,
            "LILI20", 0.20);

    private final double rate;

    private PromoCodeDiscount(double rate) {
        this.rate = rate;
    }

    /**
     * Factory method: the constructor is private so that an invalid code can never become a discount object.
     *
     * @throws InvalidPromoCodeException if the code is blank or unknown
     */
    public static PromoCodeDiscount of(String code) throws InvalidPromoCodeException {
        if (code == null || code.isBlank()) {
            throw new InvalidPromoCodeException(String.valueOf(code));
        }
        Double rate = KNOWN_CODES.get(code.trim().toUpperCase());
        if (rate == null) {
            throw new InvalidPromoCodeException(code);
        }
        return new PromoCodeDiscount(rate);
    }

    @Override
    public double discountFor(double amount) {
        return Math.round(amount * rate * 100.0) / 100.0;
    }
}
