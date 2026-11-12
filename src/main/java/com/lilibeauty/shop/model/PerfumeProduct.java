package com.lilibeauty.shop.model;

/** Perfumes are fragile glass bottles, so they cost more to ship. */
public class PerfumeProduct extends Product {

    private static final double SHIPPING_COST = 4.00;

    private final int volumeMl;

    public PerfumeProduct(String id, String name, String brand, double price, int stock, int volumeMl) {
        super(id, name, brand, price, stock);
        if (volumeMl <= 0) {
            throw new IllegalArgumentException("Volume must be positive");
        }
        this.volumeMl = volumeMl;
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    @Override
    public double getShippingCost() {
        return SHIPPING_COST;
    }

    @Override
    public String getCategory() {
        return "Perfume";
    }
}
