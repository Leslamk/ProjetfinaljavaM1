package com.lilibeauty.shop.model;

/** Creams, serums, cleansers... Light and sturdy, so cheap to ship. */
public class SkinCareProduct extends Product {

    private static final double SHIPPING_COST = 2.00;

    private final String skinType;

    public SkinCareProduct(String id, String name, String brand, double price, int stock, String skinType) {
        super(id, name, brand, price, stock);
        this.skinType = skinType;
    }

    public String getSkinType() {
        return skinType;
    }

    @Override
    public double getShippingCost() {
        return SHIPPING_COST;
    }

    @Override
    public String getCategory() {
        return "Skin care";
    }
}
