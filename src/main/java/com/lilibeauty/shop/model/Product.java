package com.lilibeauty.shop.model;

import com.lilibeauty.shop.exception.OutOfStockException;

import java.util.Objects;

/**
 * Common parent of every product sold in the shop.
 * Abstract because a "generic product" does not exist in a real cosmetics store:
 * each category has its own shipping cost and its own specific attribute.
 */
public abstract class Product {

    private final String id;
    private final String name;
    private final String brand;
    private final double price;
    private int stock;

    protected Product(String id, String name, String brand, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product id must not be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be empty");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock must not be negative");
        }
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
    }

    /** Shipping cost depends on the category (e.g. perfume is fragile), so each subclass decides. */
    public abstract double getShippingCost();

    /** Human readable category name, used for display. */
    public abstract String getCategory();

    public boolean hasStockFor(int quantity) {
        return quantity > 0 && stock >= quantity;
    }

    /**
     * Removes units from the stock.
     *
     * @throws IllegalArgumentException if quantity is not positive
     * @throws OutOfStockException      if there are not enough units left
     */
    public void decreaseStock(int quantity) throws OutOfStockException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stock < quantity) {
            throw new OutOfStockException(name, quantity, stock);
        }
        stock -= quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    /**
     * Two products are the same product if they have the same id, whatever their price or stock.
     * The id identifies the product, while price and stock can change over time.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Product other)) {
            return false;
        }
        return id.equals(other.id);
    }

    /** Must be consistent with equals(): same id, same hash code (needed by HashSet and HashMap). */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%.2f EUR, stock: %d)", id, name, brand, price, stock);
    }
}
