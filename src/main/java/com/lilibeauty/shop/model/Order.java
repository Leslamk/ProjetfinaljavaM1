package com.lilibeauty.shop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A customer's order: a list of lines and the date it was created. */
public class Order {

    private final Customer customer;
    private final LocalDate date;
    private final List<OrderLine> lines = new ArrayList<>();

    public Order(Customer customer, LocalDate date) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
        this.customer = customer;
        this.date = date;
    }

    /**
     * Adds a line. If the product is already in the order, quantities are merged
     * so the customer never sees the same product twice.
     */
    public void addLine(OrderLine line) {
        for (int i = 0; i < lines.size(); i++) {
            OrderLine existing = lines.get(i);
            if (existing.getProduct().getId().equals(line.getProduct().getId())) {
                lines.set(i, new OrderLine(existing.getProduct(), existing.getQuantity() + line.getQuantity()));
                return;
            }
        }
        lines.add(line);
    }

    /** Quantity of a given product already in this order (0 if absent). */
    public int getQuantityOf(Product product) {
        return lines.stream()
                .filter(line -> line.getProduct().getId().equals(product.getId()))
                .mapToInt(OrderLine::getQuantity)
                .sum();
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDate getDate() {
        return date;
    }

    /** Read-only view so that outside code cannot bypass addLine(). */
    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public double getSubtotal() {
        return lines.stream().mapToDouble(OrderLine::getLineTotal).sum();
    }
}
