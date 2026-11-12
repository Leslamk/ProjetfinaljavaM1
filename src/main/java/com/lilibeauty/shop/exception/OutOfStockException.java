package com.lilibeauty.shop.exception;

/**
 * Thrown when a customer asks for more units of a product than we have in stock.
 * Checked on purpose: running out of stock is an expected business case that callers must handle.
 */
public class OutOfStockException extends Exception {

    public OutOfStockException(String productName, int requested, int available) {
        super("Not enough stock for '" + productName + "': requested " + requested
                + ", available " + available);
    }
}
