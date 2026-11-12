package com.lilibeauty.shop.exception;

/** Thrown when a promo code is unknown or cannot be applied. */
public class InvalidPromoCodeException extends Exception {

    public InvalidPromoCodeException(String code) {
        super("Invalid promo code: " + code);
    }
}
