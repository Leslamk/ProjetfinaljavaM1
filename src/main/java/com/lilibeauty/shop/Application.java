package com.lilibeauty.shop;

import com.lilibeauty.shop.exception.DataLoadException;
import com.lilibeauty.shop.exception.InvalidPromoCodeException;
import com.lilibeauty.shop.exception.OutOfStockException;
import com.lilibeauty.shop.exception.ProductNotFoundException;
import com.lilibeauty.shop.model.Customer;
import com.lilibeauty.shop.model.LoyaltyTier;
import com.lilibeauty.shop.model.Order;
import com.lilibeauty.shop.model.Product;
import com.lilibeauty.shop.service.DiscountPolicyFactory;
import com.lilibeauty.shop.service.OrderService;
import com.lilibeauty.shop.service.OrderSummary;
import com.lilibeauty.shop.service.ProductCatalog;
import com.lilibeauty.shop.utils.CsvProductLoader;

import java.time.LocalDate;
import java.util.List;

/**
 * Console entry point. Kept small on purpose: it only wires the pieces together.
 * Business logic lives in the service package.
 */
public class Application {

    private static final String DATA_FILE = "products.csv";

    public static void main(String[] args) {
        try {
            List<Product> products = new CsvProductLoader().loadFromResource(DATA_FILE);
            ProductCatalog catalog = new ProductCatalog(products);

            System.out.println("*** Welcome to LiliBeauty ***");
            printCatalog(catalog);

            OrderService orderService = new OrderService();
            Customer customer = new Customer("C001", "Lili", "FR", LoyaltyTier.GOLD);

            System.out.println("\n=== Order 1: valid order (GOLD customer + promo WELCOME10) ===");
            placeOrder(orderService, catalog, customer, "P001", 2, "P005", 1, "WELCOME10");

            System.out.println("\n=== Order 2: product out of stock ===");
            placeOrder(orderService, catalog, customer, "P006", 1, "P003", 1, null);

            System.out.println("\n=== Order 3: unknown promo code ===");
            placeOrder(orderService, catalog, customer, "P003", 1, "P004", 1, "FREE100");

            System.out.println("\n=== Stock after the orders ===");
            printCatalog(catalog);
        } catch (DataLoadException e) {
            System.err.println("Cannot start the application: " + e.getMessage());
        }
    }

    private static void printCatalog(ProductCatalog catalog) {
        System.out.println("=== Catalog (" + catalog.getAll().size() + " products) ===");
        // Polymorphism: getShippingCost() and getCategory() depend on the real subclass.
        for (Product product : catalog.getAll()) {
            System.out.printf("%s | %s | shipping: %.2f EUR%n",
                    product, product.getCategory(), product.getShippingCost());
        }
    }

    /** Builds, prices and validates an order with two products; business errors are reported, not hidden. */
    private static void placeOrder(OrderService service, ProductCatalog catalog, Customer customer,
                                   String firstId, int firstQty, String secondId, int secondQty,
                                   String promoCode) {
        Order order = new Order(customer, LocalDate.now());
        try {
            service.addLine(order, catalog.getById(firstId), firstQty);
            service.addLine(order, catalog.getById(secondId), secondQty);
            OrderSummary summary = service.validate(order, DiscountPolicyFactory.forCustomer(customer, promoCode));
            System.out.printf("Order confirmed: subtotal %.2f - discount %.2f + shipping %.2f = TOTAL %.2f EUR%n",
                    summary.subtotal(), summary.discount(), summary.shipping(), summary.total());
        } catch (OutOfStockException | ProductNotFoundException | InvalidPromoCodeException e) {
            System.err.println("Order refused: " + e.getMessage());
        }
    }
}
