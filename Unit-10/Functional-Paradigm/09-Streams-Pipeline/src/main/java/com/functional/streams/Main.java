package com.functional.streams;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Demonstrates the Streams API pipeline with a sales dashboard.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Sales Dashboard (Streams Pipeline) ===\n");

        List<Sale> sales = SalesData.sample();

        // filter + mapToDouble + sum
        double totalRevenue = sales.stream()
                .mapToDouble(Sale::revenue)
                .sum();
        System.out.printf("Total revenue: $%,.2f%n%n", totalRevenue);

        // filter + mapToDouble + sum (one category)
        double electronicsRevenue = sales.stream()
                .filter(s -> s.category().equals("Electronics"))
                .mapToDouble(Sale::revenue)
                .sum();
        System.out.printf("Electronics revenue: $%,.2f%n%n", electronicsRevenue);

        // map + distinct + sorted (product names)
        List<String> products = sales.stream()
                .map(Sale::product)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("Products sold: " + products);

        // max (highest-value single transaction)
        Optional<Sale> biggestDeal = sales.stream()
                .max(Comparator.comparingDouble(Sale::revenue));
        biggestDeal.ifPresent(s -> System.out.printf(
                "%nBiggest single transaction: %s × %d = $%,.2f (%s)%n",
                s.product(), s.units(), s.revenue(), s.salesperson()));

        // filter + sorted + limit (top 3 Electronics by revenue)
        System.out.println("\nTop 3 Electronics transactions by revenue:");
        sales.stream()
                .filter(s -> s.category().equals("Electronics"))
                .sorted(Comparator.comparingDouble(Sale::revenue).reversed())
                .limit(3)
                .forEach(s -> System.out.printf("  %-20s  $%,.2f  (%s)%n",
                        s.product(), s.revenue(), s.region()));

        // flatMap — all salesperson names with duplicates, then distinct count
        long uniqueReps = sales.stream()
                .map(Sale::salesperson)
                .distinct()
                .count();
        System.out.printf("%nUnique salespeople: %d%n", uniqueReps);

        // reduce — total units sold
        int totalUnits = sales.stream()
                .mapToInt(Sale::units)
                .reduce(0, Integer::sum);
        System.out.printf("Total units sold:   %d%n", totalUnits);
    }
}
