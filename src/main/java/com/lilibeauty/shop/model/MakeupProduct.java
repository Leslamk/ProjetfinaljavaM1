package com.lilibeauty.shop.model;

/** Lipstick, foundation, mascara... Small items, cheapest to ship. */
public class MakeupProduct extends Product {

    private static final double SHIPPING_COST = 1.50;

    private final String shade;

    public MakeupProduct(String id, String name, String brand, double price, int stock, String shade) {
        super(id, name, brand, price, stock);
        this.shade = shade;
    }

    public String getShade() {
        return shade;
    }

    @Override
    public double getShippingCost() {
        return SHIPPING_COST;
    }

    @Override
    public String getCategory() {
        return "Makeup";
    }
}
