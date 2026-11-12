package com.lilibeauty.shop.service;

import com.lilibeauty.shop.exception.InvalidPromoCodeException;
import com.lilibeauty.shop.exception.OutOfStockException;
import com.lilibeauty.shop.model.Customer;
import com.lilibeauty.shop.model.LoyaltyTier;
import com.lilibeauty.shop.model.MakeupProduct;
import com.lilibeauty.shop.model.Order;
import com.lilibeauty.shop.model.PerfumeProduct;
import com.lilibeauty.shop.model.SkinCareProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private static final double DELTA = 0.001;

    private final OrderService service = new OrderService();

    private SkinCareProduct cream;   // 12.50 EUR, shipping 2.00
    private MakeupProduct lipstick;  // 9.99 EUR, shipping 1.50
    private PerfumeProduct perfume;  // 79.00 EUR, shipping 4.00, only 5 in stock

    @BeforeEach
    void setUp() {
        cream = new SkinCareProduct("P001", "Cream", "Nivea", 12.50, 20, "dry");
        lipstick = new MakeupProduct("P003", "Lipstick", "Maybelline", 9.99, 30, "red");
        perfume = new PerfumeProduct("P005", "Perfume", "Dior", 79.00, 5, 50);
    }

    private Order orderFor(LoyaltyTier tier) {
        return new Order(new Customer("C1", "Test", "FR", tier), LocalDate.of(2026, 10, 23));
    }

    // ---- normal situations ----

    @Test
    void shouldCalculateTotalWithShippingWhenNoDiscount() throws OutOfStockException {
        Order order = orderFor(LoyaltyTier.STANDARD);
        service.addLine(order, cream, 2);     // 25.00
        service.addLine(order, lipstick, 1);  // 9.99

        OrderSummary summary = service.summarize(order, DiscountPolicyFactory.loyaltyOnly(order.getCustomer()));

        assertEquals(34.99, summary.subtotal(), DELTA);
        assertEquals(0.00, summary.discount(), DELTA);
        assertEquals(3.50, summary.shipping(), DELTA);   // 2.00 + 1.50
        assertEquals(38.49, summary.total(), DELTA);
    }

    @Test
    void shouldApplyGoldLoyaltyDiscount() throws OutOfStockException {
        Order order = orderFor(LoyaltyTier.GOLD);
        service.addLine(order, perfume, 1);   // 79.00

        OrderSummary summary = service.summarize(order, DiscountPolicyFactory.loyaltyOnly(order.getCustomer()));

        assertEquals(7.90, summary.discount(), DELTA);
        assertEquals(71.10, summary.total(), DELTA);     // free shipping above 60 EUR
    }

    @Test
    void shouldApplyLoyaltyThenPromoCodeOnTheRemainingAmount() throws Exception {
        Order order = orderFor(LoyaltyTier.GOLD);
        service.addLine(order, perfume, 1);   // 79.00 -> 71.10 -> 63.99

        OrderSummary summary = service.summarize(order,
                DiscountPolicyFactory.forCustomer(order.getCustomer(), "welcome10"));

        assertEquals(15.01, summary.discount(), DELTA);
        assertEquals(63.99, summary.total(), DELTA);
    }

    @Test
    void shouldMergeQuantitiesWhenSameProductIsAddedTwice() throws OutOfStockException {
        Order order = orderFor(LoyaltyTier.STANDARD);
        service.addLine(order, cream, 2);
        service.addLine(order, cream, 3);

        assertEquals(1, order.getLines().size());
        assertEquals(5, order.getQuantityOf(cream));
    }

    @Test
    void shouldDecreaseStockWhenOrderIsValidated() throws OutOfStockException {
        Order order = orderFor(LoyaltyTier.STANDARD);
        service.addLine(order, perfume, 2);

        service.validate(order, DiscountPolicyFactory.loyaltyOnly(order.getCustomer()));

        assertEquals(3, perfume.getStock());
    }

    // ---- edge cases ----

    @Test
    void shouldGiveFreeShippingWhenAmountIsExactlyTheThreshold() throws OutOfStockException {
        MakeupProduct sixtyEuros = new MakeupProduct("T1", "Palette", "Test", 60.00, 5, "mix");
        Order order = orderFor(LoyaltyTier.STANDARD);
        service.addLine(order, sixtyEuros, 1);

        OrderSummary summary = service.summarize(order, DiscountPolicyFactory.loyaltyOnly(order.getCustomer()));

        assertEquals(0.00, summary.shipping(), DELTA);
        assertEquals(60.00, summary.total(), DELTA);
    }

    @Test
    void shouldChargeShippingJustBelowTheThreshold() throws OutOfStockException {
        MakeupProduct almost = new MakeupProduct("T2", "Palette", "Test", 59.99, 5, "mix");
        Order order = orderFor(LoyaltyTier.STANDARD);
        service.addLine(order, almost, 1);

        OrderSummary summary = service.summarize(order, DiscountPolicyFactory.loyaltyOnly(order.getCustomer()));

        assertEquals(1.50, summary.shipping(), DELTA);
        assertEquals(61.49, summary.total(), DELTA);
    }

    @Test
    void shouldAllowOrderingExactlyTheAvailableStock() throws OutOfStockException {
        Order order = orderFor(LoyaltyTier.STANDARD);

        service.addLine(order, perfume, 5);   // stock is 5

        assertEquals(5, order.getQuantityOf(perfume));
    }

    @Test
    void shouldReturnZeroTotalForEmptyOrder() {
        Order order = orderFor(LoyaltyTier.GOLD);

        OrderSummary summary = service.summarize(order, DiscountPolicyFactory.loyaltyOnly(order.getCustomer()));

        assertEquals(0.00, summary.total(), DELTA);
    }

    // ---- invalid situations ----

    @Test
    void shouldThrowWhenQuantityExceedsStock() {
        Order order = orderFor(LoyaltyTier.STANDARD);

        assertThrows(OutOfStockException.class, () -> service.addLine(order, perfume, 6));
    }

    @Test
    void shouldThrowWhenCumulatedQuantityExceedsStock() throws OutOfStockException {
        Order order = orderFor(LoyaltyTier.STANDARD);
        service.addLine(order, perfume, 3);

        assertThrows(OutOfStockException.class, () -> service.addLine(order, perfume, 3));
    }

    @Test
    void shouldThrowWhenQuantityIsZeroOrNegative() {
        Order order = orderFor(LoyaltyTier.STANDARD);

        assertThrows(IllegalArgumentException.class, () -> service.addLine(order, cream, 0));
        assertThrows(IllegalArgumentException.class, () -> service.addLine(order, cream, -2));
    }

    @Test
    void shouldThrowWhenPromoCodeIsUnknown() {
        Customer customer = new Customer("C1", "Test", "FR", LoyaltyTier.STANDARD);

        assertThrows(InvalidPromoCodeException.class, () -> DiscountPolicyFactory.forCustomer(customer, "FREE100"));
    }

    @Test
    void shouldThrowWhenValidatingAnEmptyOrder() {
        Order order = orderFor(LoyaltyTier.STANDARD);
        List<DiscountPolicy> policies = DiscountPolicyFactory.loyaltyOnly(order.getCustomer());

        assertThrows(IllegalStateException.class, () -> service.validate(order, policies));
    }

    @Test
    void shouldLeaveStockUntouchedWhenValidationFails() throws OutOfStockException {
        Order order = orderFor(LoyaltyTier.STANDARD);
        service.addLine(order, cream, 2);
        service.addLine(order, perfume, 5);
        perfume.decreaseStock(4);             // someone else bought 4 meanwhile: only 1 left
        List<DiscountPolicy> policies = DiscountPolicyFactory.loyaltyOnly(order.getCustomer());

        assertThrows(OutOfStockException.class, () -> service.validate(order, policies));

        assertEquals(20, cream.getStock());   // the cream stock must not have been decreased
    }
}
