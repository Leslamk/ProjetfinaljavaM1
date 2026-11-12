package com.lilibeauty.shop.service;

import com.lilibeauty.shop.exception.OutOfStockException;
import com.lilibeauty.shop.model.Order;
import com.lilibeauty.shop.model.OrderLine;
import com.lilibeauty.shop.model.Product;

import java.util.List;

/** Business rules about orders: adding products, pricing, and validating (paying) an order. */
public class OrderService {

    /** Orders at or above this amount (after discounts) ship for free. */
    public static final double FREE_SHIPPING_THRESHOLD = 60.00;

    /**
     * Adds a product to the order after checking that the stock covers everything
     * the customer wants, including what is already in the order.
     * The stock itself is only decreased when the order is validated.
     *
     * @throws OutOfStockException      if the total requested exceeds the stock
     * @throws IllegalArgumentException if the quantity is not positive
     */
    public void addLine(Order order, Product product, int quantity) throws OutOfStockException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        int totalWanted = order.getQuantityOf(product) + quantity;
        if (!product.hasStockFor(totalWanted)) {
            throw new OutOfStockException(product.getName(), totalWanted, product.getStock());
        }
        order.addLine(new OrderLine(product, quantity));
    }

    /**
     * Computes the price breakdown without changing anything.
     * Discounts are applied one after the other, each on the amount left by the previous one,
     * so the price can never become negative.
     */
    public OrderSummary summarize(Order order, List<DiscountPolicy> policies) {
        double subtotal = round2(order.getSubtotal());

        double afterDiscounts = subtotal;
        for (DiscountPolicy policy : policies) {
            afterDiscounts -= policy.discountFor(afterDiscounts);
        }
        afterDiscounts = round2(afterDiscounts);
        double discount = round2(subtotal - afterDiscounts);

        double shipping = 0.0;
        if (!order.getLines().isEmpty() && afterDiscounts < FREE_SHIPPING_THRESHOLD) {
            shipping = round2(order.getLines().stream()
                    .mapToDouble(line -> line.getProduct().getShippingCost())
                    .sum());
        }
        return new OrderSummary(subtotal, discount, shipping, round2(afterDiscounts + shipping));
    }

    /**
     * Validates the order: checks the stock of every line, then decreases it.
     * All checks happen before any change, so a failed validation leaves the stock untouched.
     *
     * @throws IllegalStateException if the order is empty
     * @throws OutOfStockException   if a product no longer has enough stock
     */
    public OrderSummary validate(Order order, List<DiscountPolicy> policies) throws OutOfStockException {
        if (order.getLines().isEmpty()) {
            throw new IllegalStateException("Cannot validate an empty order");
        }
        for (OrderLine line : order.getLines()) {
            Product product = line.getProduct();
            if (!product.hasStockFor(line.getQuantity())) {
                throw new OutOfStockException(product.getName(), line.getQuantity(), product.getStock());
            }
        }
        for (OrderLine line : order.getLines()) {
            line.getProduct().decreaseStock(line.getQuantity());
        }
        return summarize(order, policies);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
