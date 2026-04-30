package com.functional.closures;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Demonstrates closures through counter, accumulator, and rate-limiter factories.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Counter Factory (Closures) ===\n");

        // Each counter has its own private state
        IntSupplier orderIds    = CounterFactory.counter(1000, 1);
        IntSupplier invoiceIds  = CounterFactory.counter(9000, 10);
        IntSupplier evens       = CounterFactory.counter(0, 2);

        System.out.println("Order IDs (start 1000, step 1):");
        for (int i = 0; i < 5; i++) System.out.print("  " + orderIds.getAsInt());

        System.out.println("\nInvoice IDs (start 9000, step 10):");
        for (int i = 0; i < 5; i++) System.out.print("  " + invoiceIds.getAsInt());

        System.out.println("\nEven numbers:");
        for (int i = 0; i < 6; i++) System.out.print("  " + evens.getAsInt());

        // Accumulators — each tracks its own running total
        System.out.println("\n\nCart accumulators (independent):");
        Accumulator cart1 = CounterFactory.accumulator();
        Accumulator cart2 = CounterFactory.accumulator();

        System.out.printf("  Cart1 +$10.99 → total $%.2f%n", cart1.add(10.99));
        System.out.printf("  Cart2 +$ 5.50 → total $%.2f%n", cart2.add(5.50));
        System.out.printf("  Cart1 +$24.00 → total $%.2f%n", cart1.add(24.00));
        System.out.printf("  Cart2 +$12.75 → total $%.2f%n", cart2.add(12.75));

        // Rate limiter
        System.out.println("\nRate limiter (max 3 calls per 1 second):");
        Supplier<Boolean> limiter = CounterFactory.rateLimiter(3, 1000);
        for (int i = 0; i < 5; i++) {
            System.out.printf("  Call %d: %s%n", i + 1, limiter.get() ? "ALLOWED" : "BLOCKED");
        }
    }
}
