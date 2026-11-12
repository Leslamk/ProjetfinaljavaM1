package com.lilibeauty.shop.utils;

import com.lilibeauty.shop.exception.DataLoadException;
import com.lilibeauty.shop.model.MakeupProduct;
import com.lilibeauty.shop.model.PerfumeProduct;
import com.lilibeauty.shop.model.Product;
import com.lilibeauty.shop.model.SkinCareProduct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads products from a CSV file: type,id,name,brand,price,stock,extra
 * Its only job is turning text lines into Product objects (single responsibility).
 */
public class CsvProductLoader {

    private static final int EXPECTED_COLUMNS = 7;

    /** Loads a CSV file located in src/main/resources (on the classpath). */
    public List<Product> loadFromResource(String resourceName) throws DataLoadException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            throw new DataLoadException("Data file not found: " + resourceName);
        }
        return load(stream);
    }

    /** Reads any stream; kept public so unit tests can feed it text without touching the disk. */
    public List<Product> load(InputStream input) throws DataLoadException {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // header line, ignored
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                products.add(parseLine(line, lineNumber));
            }
        } catch (IOException e) {
            throw new DataLoadException("Could not read the data file", e);
        }
        return products;
    }

    private Product parseLine(String line, int lineNumber) throws DataLoadException {
        String[] parts = line.split(",", -1);
        if (parts.length != EXPECTED_COLUMNS) {
            throw new DataLoadException("Line " + lineNumber + ": expected " + EXPECTED_COLUMNS
                    + " columns but found " + parts.length);
        }
        try {
            String type = parts[0].trim().toUpperCase();
            String id = parts[1].trim();
            String name = parts[2].trim();
            String brand = parts[3].trim();
            double price = Double.parseDouble(parts[4].trim());
            int stock = Integer.parseInt(parts[5].trim());
            String extra = parts[6].trim();

            return switch (type) {
                case "SKINCARE" -> new SkinCareProduct(id, name, brand, price, stock, extra);
                case "MAKEUP" -> new MakeupProduct(id, name, brand, price, stock, extra);
                case "PERFUME" -> new PerfumeProduct(id, name, brand, price, stock, Integer.parseInt(extra));
                default -> throw new DataLoadException("Line " + lineNumber + ": unknown product type '" + type + "'");
            };
        } catch (NumberFormatException e) {
            throw new DataLoadException("Line " + lineNumber + ": invalid number", e);
        } catch (IllegalArgumentException e) {
            // Raised by the model constructors (negative price, empty name...)
            throw new DataLoadException("Line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
