package com.lilibeauty.shop.model;

public class Customer {

    private final String id;
    private final String name;
    private final String country;
    private final LoyaltyTier tier;

    public Customer(String id, String name, String country, LoyaltyTier tier) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Customer id must not be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name must not be empty");
        }
        if (tier == null) {
            throw new IllegalArgumentException("Loyalty tier must not be null");
        }
        this.id = id;
        this.name = name;
        this.country = country;
        this.tier = tier;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public LoyaltyTier getTier() {
        return tier;
    }
}
