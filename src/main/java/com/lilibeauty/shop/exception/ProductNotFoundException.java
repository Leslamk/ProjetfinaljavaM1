package com.lilibeauty.shop.exception;

/** Thrown when a product id does not exist in the catalog. */
public class ProductNotFoundException extends Exception {

    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}
