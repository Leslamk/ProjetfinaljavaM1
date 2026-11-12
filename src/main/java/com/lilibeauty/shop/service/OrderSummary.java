package com.lilibeauty.shop.service;

/** The price breakdown of an order, all amounts in EUR. A record fits: it is plain immutable data. */
public record OrderSummary(double subtotal, double discount, double shipping, double total) {
}
