package com.lilibeauty.shop.model;

import com.lilibeauty.shop.exception.OutOfStockException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

    private MakeupProduct newLipstick(int stock) {
        return new MakeupProduct("P003", "Lipstick", "Maybelline", 9.99, stock, "red");
    }

    @Test
    void shouldDecreaseStock() throws OutOfStockException {
        MakeupProduct lipstick = newLipstick(10);

        lipstick.decreaseStock(4);

        assertEquals(6, lipstick.getStock());
    }

    @Test
    void shouldAllowDecreasingStockDownToZero() throws OutOfStockException {
        MakeupProduct lipstick = newLipstick(3);

        lipstick.decreaseStock(3);

        assertEquals(0, lipstick.getStock());
        assertFalse(lipstick.hasStockFor(1));
    }

    @Test
    void shouldThrowWhenDecreasingMoreThanStock() {
        MakeupProduct lipstick = newLipstick(2);

        assertThrows(OutOfStockException.class, () -> lipstick.decreaseStock(3));
        assertEquals(2, lipstick.getStock());
    }

    @Test
    void shouldRejectNegativePriceAndNegativeStock() {
        assertThrows(IllegalArgumentException.class,
                () -> new MakeupProduct("P1", "Lipstick", "X", -1.0, 5, "red"));
        assertThrows(IllegalArgumentException.class,
                () -> new MakeupProduct("P1", "Lipstick", "X", 5.0, -1, "red"));
    }

    @Test
    void shouldReportStockAvailability() {
        MakeupProduct lipstick = newLipstick(5);

        assertTrue(lipstick.hasStockFor(5));
        assertFalse(lipstick.hasStockFor(6));
        assertFalse(lipstick.hasStockFor(0));
    }

    @Test
    void shouldConsiderTwoProductsWithTheSameIdAsEqual() {
        MakeupProduct first = new MakeupProduct("P003", "Lipstick", "Maybelline", 9.99, 30, "red");
        MakeupProduct second = new MakeupProduct("P003", "Lipstick (new price)", "Maybelline", 12.00, 5, "pink");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldConsiderTwoProductsWithDifferentIdsAsDifferent() {
        MakeupProduct first = new MakeupProduct("P003", "Lipstick", "Maybelline", 9.99, 30, "red");
        MakeupProduct second = new MakeupProduct("P004", "Lipstick", "Maybelline", 9.99, 30, "red");

        assertNotEquals(first, second);
    }

    @Test
    void shouldNotBeEqualToNullOrToAnotherType() {
        MakeupProduct lipstick = newLipstick(1);

        assertNotEquals(null, lipstick);
        assertNotEquals("P003", lipstick);
    }

    @Test
    void shouldRemoveDuplicatesInAHashSet() {
        Set<Product> products = new HashSet<>();
        products.add(new MakeupProduct("P003", "Lipstick", "Maybelline", 9.99, 30, "red"));
        products.add(new MakeupProduct("P003", "Lipstick", "Maybelline", 9.99, 30, "red"));

        assertEquals(1, products.size());
    }

    @Test
    void shouldComputeShippingAccordingToTheRealSubclass() {
        List<Product> products = List.of(
                new SkinCareProduct("P1", "Cream", "Nivea", 12.5, 1, "dry"),
                newLipstick(1),
                new PerfumeProduct("P5", "Perfume", "Dior", 79.0, 1, 50));

        assertEquals(2.00, products.get(0).getShippingCost(), 0.001);
        assertEquals(1.50, products.get(1).getShippingCost(), 0.001);
        assertEquals(4.00, products.get(2).getShippingCost(), 0.001);
    }
}
