package com.functional.collectors;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates Collectors with an order reporting system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Order Report (Collectors) ===\n");

        List<Order> orders = List.of(
            new Order(1,  "Alice", "North", "Laptop Pro",     "Electronics", 1299.99, true),
            new Order(2,  "Bob",   "South", "Desk Chair",     "Furniture",    449.00, false),
            new Order(3,  "Carol", "East",  "Wireless Mouse", "Electronics",   59.98, true),
            new Order(4,  "Dave",  "West",  "Standing Desk",  "Furniture",    799.00, true),
            new Order(5,  "Alice", "North", "Monitor 4K",     "Electronics",  599.00, false),
            new Order(6,  "Bob",   "South", "Keyboard",       "Electronics",   89.99, true),
            new Order(7,  "Carol", "East",  "Bookshelf",      "Furniture",    249.00, true),
            new Order(8,  "Dave",  "West",  "Laptop Pro",     "Electronics", 1299.99, false),
            new Order(9,  "Alice", "North", "Desk Chair",     "Furniture",    449.00, true),
            new Order(10, "Bob",   "South", "Monitor 4K",     "Electronics",  599.00, true)
        );

        // toList — simplest collector
        List<String> customerNames = orders.stream()
                .map(Order::customer)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("Customers: " + customerNames);

        // groupingBy — partition into a Map<key, List<value>>
        Map<String, List<Order>> byCategory = orders.stream()
                .collect(Collectors.groupingBy(Order::category));
        System.out.println("\nOrders by category:");
        byCategory.forEach((cat, list) ->
                System.out.printf("  %-15s %d orders%n", cat, list.size()));

        // groupingBy with downstream collector — sum revenue per region
        Map<String, Double> revenueByRegion = orders.stream()
                .collect(Collectors.groupingBy(Order::region,
                         Collectors.summingDouble(Order::total)));
        System.out.println("\nRevenue by region:");
        revenueByRegion.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %-10s $%,.2f%n", e.getKey(), e.getValue()));

        // partitioningBy — split into two groups (shipped vs. pending)
        Map<Boolean, List<Order>> shipped = orders.stream()
                .collect(Collectors.partitioningBy(Order::shipped));
        System.out.printf("%nShipped: %d   Pending: %d%n",
                shipped.get(true).size(), shipped.get(false).size());

        // counting downstream collector
        Map<String, Long> countByCustomer = orders.stream()
                .collect(Collectors.groupingBy(Order::customer, Collectors.counting()));
        System.out.println("\nOrders per customer:");
        countByCustomer.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %-8s %d%n", e.getKey(), e.getValue()));

        // summarizingDouble — statistics in one pass
        DoubleSummaryStatistics stats = orders.stream()
                .collect(Collectors.summarizingDouble(Order::total));
        System.out.printf("%nOrder statistics:%n  Count: %d  Min: $%.2f  Max: $%.2f  Avg: $%.2f%n",
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());

        // joining — build a comma-separated product list
        String productList = orders.stream()
                .map(Order::product)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        System.out.println("\nProducts: " + productList);
    }
}
