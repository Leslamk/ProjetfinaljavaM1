# LiliBeauty

A console application that simulates the orders of **LiliBeauty**, an online cosmetics shop.
It loads the product catalog from a CSV file, builds customer orders, applies discounts,
calculates shipping costs and updates the stock when an order is validated.

Individual final project - Object Oriented Programming in Java (JUNIA, M1).

## Table of contents

1. [How to run the application](#1-how-to-run-the-application)
2. [Features](#2-features)
3. [Project structure and main classes](#3-project-structure-and-main-classes)
4. [Technical decisions](#4-technical-decisions)
5. [OOP concepts: where to find them](#5-oop-concepts-where-to-find-them)
6. [Custom exceptions](#6-custom-exceptions)
7. [Data loading](#7-data-loading)
8. [SOLID principles](#8-solid-principles)
9. [How to run the tests](#9-how-to-run-the-tests)
10. [Known limitations](#10-known-limitations)

## 1. How to run the application

**Requirements:** JDK 17 or later, Maven 3.8+ (IntelliJ IDEA includes Maven).

**From IntelliJ IDEA**
1. `File > Open`, then select the `pom.xml` file.
2. Wait for Maven to download the dependencies (or click *Reload All Maven Projects*).
3. Open `src/main/java/com/lilibeauty/shop/Application.java` and click the green arrow next to `main`.

**From the command line**
```
mvn compile exec:java
```

The demo creates a GOLD customer and runs three orders: one valid order with a promo code,
one refused because of an out-of-stock product, and one refused because of an invalid promo code.
The catalog is printed before and after, so you can see the stock decrease.

## 2. Features

- Load the product catalog from `src/main/resources/products.csv` at startup.
- Three product categories (skin care, makeup, perfume), each with its own shipping cost.
- Add products to an order, with a stock check that includes what is already in the order.
- Discounts: loyalty discount (STANDARD 0 %, SILVER 5 %, GOLD 10 %) and promo codes
  (`WELCOME10` = 10 %, `LILI20` = 20 %). Discounts are applied one after the other.
- Shipping: the sum of the shipping costs of the ordered products, **free when the amount
  after discounts is at least 60 EUR**.
- Order validation: the stock is decreased only if every line can be satisfied.
- Search products by id (`Optional`) or by brand (stream).

## 3. Project structure and main classes

```
src/main/java/com/lilibeauty/shop
|-- Application.java            Entry point: wires the pieces together, no business logic
|-- model/                      The "things" of the shop
|   |-- Product                 Abstract parent of all products
|   |-- SkinCareProduct, MakeupProduct, PerfumeProduct
|   |-- Customer, LoyaltyTier (enum)
|   |-- Order, OrderLine
|-- service/                    The business rules
|   |-- DiscountPolicy          Interface: a rule that reduces a price
|   |-- LoyaltyDiscount, PromoCodeDiscount
|   |-- DiscountPolicyFactory   Chooses the discounts that apply to a customer
|   |-- OrderService            Adds lines, computes totals, validates orders
|   |-- OrderSummary            Record: subtotal, discount, shipping, total
|   |-- ProductCatalog          Finds products (Map, Optional, stream)
|-- exception/                  Custom exceptions
|-- utils/CsvProductLoader      Reads the CSV file and creates the products
src/main/resources/products.csv Initial data
src/test/java/...               JUnit 5 tests
```

| Class | Responsibility |
|---|---|
| `Product` | Common data (id, name, brand, price, stock) and stock handling |
| `Order` / `OrderLine` | A customer's order and its lines (product + quantity) |
| `OrderService` | Business rules: stock check, price calculation, validation |
| `DiscountPolicy` | Contract for any discount rule |
| `CsvProductLoader` | Technical task: turn CSV lines into `Product` objects |
| `ProductCatalog` | Lookup of products |

## 4. Technical decisions

**1. Checked exceptions for business errors, unchecked exceptions for invalid arguments.**
`OutOfStockException`, `InvalidPromoCodeException`... extend `Exception`: they are expected situations
that the caller must handle (the `Application` shows a clear message and continues).
Invalid arguments (negative price, zero quantity) and invalid states (validating an empty order)
use `IllegalArgumentException` / `IllegalStateException`, because they are programming errors.
The rule is applied consistently in the whole application.

**2. Discounts are a `DiscountPolicy` interface (Strategy pattern).**
Loyalty and promo discounts are two interchangeable implementations. Adding a new discount
(for example a seasonal one) means adding a new class, without modifying `OrderService`.
`DiscountPolicyFactory` is the only place that knows the concrete classes (simple factory).

**3. `validate()` checks the stock of every line before decreasing any stock.**
If the third product of an order is out of stock, the first two must not be decreased.
This is tested in `shouldLeaveStockUntouchedWhenValidationFails`.

**4. `Product` defines equality by id (`equals` / `hashCode`).**
The id identifies a product, while the price and the stock can change.
Because `equals` and `hashCode` are consistent, products behave correctly in a `HashSet`.

**5. `CsvProductLoader` can read any `InputStream`.**
The file reading is separated from the parsing, so tests can feed it text directly
without touching the disk.

## 5. OOP concepts: where to find them

| Concept | Where |
|---|---|
| **Encapsulation** | Private fields with getters in every model class; `Order.getLines()` returns an unmodifiable list |
| **Inheritance** | `SkinCareProduct`, `MakeupProduct`, `PerfumeProduct` extend `Product` |
| **Abstract class** | `Product`: a "generic product" does not exist in a real shop, and each category must define its own `getShippingCost()` and `getCategory()` |
| **Polymorphism** | `List<Product>` is used everywhere; `getShippingCost()` and `getCategory()` run the version of the real subclass (see `Application.printCatalog` and `OrderService.summarize`) |
| **Interface** | `DiscountPolicy`: a contract describing what a discount can do, implemented by `LoyaltyDiscount` and `PromoCodeDiscount` |
| **Composition** | An `Order` has a `Customer` and a list of `OrderLine`; an `OrderLine` has a `Product` |
| **Enum** | `LoyaltyTier` (the set of tiers is fixed, each has a discount rate) |
| **Collections** | `Map<String, Product>` in `ProductCatalog`, `List` for orders and discounts, `Set` in the tests |
| **Streams / lambdas** | `ProductCatalog.findByBrand`, `Order.getSubtotal`, `OrderService.summarize` |
| **Optional** | `ProductCatalog.findById` returns an `Optional<Product>` instead of `null` |
| **Records** | `OrderSummary` (immutable data carrier) |
| **Dates** | `Order` stores a `LocalDate` |

## 6. Custom exceptions

| Exception | Thrown when |
|---|---|
| `OutOfStockException` | The requested quantity is higher than the stock (when adding a line or validating) |
| `ProductNotFoundException` | A product id does not exist in the catalog |
| `InvalidPromoCodeException` | A promo code is blank or unknown |
| `DataLoadException` | The data file is missing, unreadable or contains an invalid line (it keeps the original cause) |

All of them are checked exceptions. Invalid arguments use the standard `IllegalArgumentException`,
and validating an empty order uses `IllegalStateException`.

## 7. Data loading

At startup, `CsvProductLoader` reads `src/main/resources/products.csv`:

```
type,id,name,brand,price,stock,extra
SKINCARE,P001,Crème hydratante,Nivea,12.50,20,peau sèche
PERFUME,P005,Eau de parfum,Dior,79.00,5,50
```

- `type` decides which subclass is created (`SKINCARE`, `MAKEUP`, `PERFUME`).
- `extra` is the skin type, the shade or the volume in ml, depending on the type.
- The reader uses try-with-resources. A missing file, a wrong number of columns, a number that
  cannot be parsed, an unknown type or an invalid value (negative price...) all become a
  `DataLoadException` that contains the line number.
- Product `P006` has a stock of 0 on purpose, to demonstrate the out-of-stock case.

## 8. SOLID principles

- **S - Single Responsibility:** each class has one role. `Product` and `Order` hold data and simple
  rules about themselves, `OrderService` holds the order rules, `CsvProductLoader` only reads files,
  `Application` only wires and displays.
- **O - Open/Closed:** a new discount is a new class implementing `DiscountPolicy`;
  `OrderService` does not change.
- **L - Liskov Substitution:** every `Product` subclass can be used wherever a `Product` is expected.
  No subclass throws for a normal operation of the parent (stock, price, shipping).
- **I - Interface Segregation:** `DiscountPolicy` has a single method, so implementations
  are never forced to provide something they cannot do.
- **D - Dependency Inversion:** `OrderService.summarize()` and `validate()` receive a
  `List<DiscountPolicy>`: they depend on the abstraction, not on `LoyaltyDiscount` or
  `PromoCodeDiscount`. The application chooses the implementations.

## 9. How to run the tests

```
mvn test
```

In IntelliJ: right-click `src/test/java`, then **Run 'All Tests'**.

The tests use JUnit 5 and follow the Arrange - Act - Assert structure:

| Test class | What it covers |
|---|---|
| `OrderServiceTest` | Totals, loyalty and promo discounts, free-shipping threshold (exactly at and just below), merging of quantities, stock decrease, empty order, stock untouched after a failed validation |
| `ProductTest` | Stock handling, invalid values, availability, shipping cost by subclass, `equals` / `hashCode` |
| `CsvProductLoaderTest` | Loading the default file, the right subclass per type, missing file, wrong columns, invalid number, unknown type |

Each behaviour has normal cases, edge cases (exact stock, exact free-shipping threshold, empty order)
and invalid cases (`assertThrows`).

## 10. Known limitations

- Promo codes are defined in the code (`PromoCodeDiscount`); a real shop would load them from a file or database.
- Orders are not saved: the stock returns to its initial values at each launch.
- Only a console interface is provided.
