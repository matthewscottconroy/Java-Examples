package com.functional.result;

import java.util.List;

/**
 * Demonstrates the Result type with a CSV product importer.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== CSV Parser (Result Type) ===\n");

        // Single row parsing
        System.out.println("--- Single row parsing ---");
        List<String> tests = List.of(
                "1,Laptop Pro,1299.99,12",      // valid
                "2,,449.00,5",                   // blank name
                "3,Monitor,-599.00,8",           // negative price
                "4,Keyboard,abc,20",             // invalid price
                "bad,Chair,249.00,3",            // invalid ID
                ""                               // empty row
        );
        for (String row : tests) {
            Result<Product> r = CsvParser.parseRow(row);
            System.out.printf("  %-30s → %s%n",
                    row.isEmpty() ? "(empty)" : row, r);
        }

        // Batch parsing — collect all results
        System.out.println("\n--- Batch parsing ---");
        List<String> batch = List.of(
                "1,Laptop Pro,1299.99,12",
                "2,Desk Chair,449.00,5",
                "3,Monitor,-599.00,8",         // bad price
                "4,Keyboard,89.99,20",
                "five,Headset,79.99,15",       // bad ID
                "6,Webcam,99.99,30"
        );

        ParseSummary summary = CsvParser.parseAll(batch);
        System.out.printf("Parsed %d/%d rows successfully%n",
                summary.successCount(), batch.size());

        System.out.println("Products:");
        summary.products().forEach(p -> System.out.printf(
                "  [%d] %-15s $%.2f  stock:%d%n", p.id(), p.name(), p.price(), p.stock()));

        System.out.println("Errors:");
        summary.errors().forEach(e -> System.out.println("  " + e));

        // Composing with map and flatMap
        System.out.println("\n--- Composing with map ---");
        Result<Double> discounted = CsvParser.parseRow("1,Laptop,1000.00,5")
                .map(p -> p.price() * 0.90);
        System.out.println("Discounted price: " + discounted);

        Result<Double> errorPropagated = CsvParser.parseRow("bad,Laptop,1000.00,5")
                .map(p -> p.price() * 0.90);
        System.out.println("Error propagated: " + errorPropagated);
    }
}
