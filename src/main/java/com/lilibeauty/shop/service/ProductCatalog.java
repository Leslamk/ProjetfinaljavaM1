package com.lilibeauty.shop.service;

import com.lilibeauty.shop.exception.ProductNotFoundException;
import com.lilibeauty.shop.model.Product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Holds all products and answers lookup questions. A Map gives fast access by id. */
public class ProductCatalog {

    private final Map<String, Product> productsById = new LinkedHashMap<>();

    public ProductCatalog(Collection<Product> products) {
        for (Product product : products) {
            productsById.put(product.getId(), product);
        }
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(productsById.get(id));
    }

    /** Same as findById but a missing product is an error for the caller. */
    public Product getById(String id) throws ProductNotFoundException {
        return findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Product> findByBrand(String brand) {
        return productsById.values().stream()
                .filter(p -> p.getBrand().equalsIgnoreCase(brand))
                .toList();
    }

    public List<Product> getAll() {
        return new ArrayList<>(productsById.values());
    }
}
