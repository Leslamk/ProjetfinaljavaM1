package com.lilibeauty.shop.utils;

import com.lilibeauty.shop.exception.DataLoadException;
import com.lilibeauty.shop.model.PerfumeProduct;
import com.lilibeauty.shop.model.Product;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvProductLoaderTest {

    private static final String HEADER = "type,id,name,brand,price,stock,extra\n";

    private final CsvProductLoader loader = new CsvProductLoader();

    private InputStream csv(String body) {
        return new ByteArrayInputStream((HEADER + body).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldLoadAllProductsFromDefaultDataFile() throws DataLoadException {
        List<Product> products = loader.loadFromResource("products.csv");
        assertEquals(6, products.size());
    }

    @Test
    void shouldCreateTheRightSubclassForEachType() throws DataLoadException {
        List<Product> products = loader.load(csv("PERFUME,P1,Eau,Dior,79.00,5,50\n"));
        assertInstanceOf(PerfumeProduct.class, products.get(0));
    }

    @Test
    void shouldThrowWhenFileDoesNotExist() {
        assertThrows(DataLoadException.class, () -> loader.loadFromResource("missing.csv"));
    }

    @Test
    void shouldThrowWhenLineHasWrongNumberOfColumns() {
        assertThrows(DataLoadException.class, () -> loader.load(csv("MAKEUP,P1,Rouge\n")));
    }

    @Test
    void shouldThrowWhenPriceIsNotANumber() {
        assertThrows(DataLoadException.class, () -> loader.load(csv("MAKEUP,P1,Rouge,Maybelline,abc,5,rouge\n")));
    }

    @Test
    void shouldThrowWhenTypeIsUnknown() {
        assertThrows(DataLoadException.class, () -> loader.load(csv("FOOD,P1,Cake,X,5.0,5,none\n")));
    }
}
