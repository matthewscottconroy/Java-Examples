package com.algorithms.dp.knapsack;

import java.util.List;

/**
 * Demonstrates 0/1 knapsack on a cloud resource allocation scenario.
 *
 * A data centre has a fixed amount of RAM. Services have different memory
 * requirements and different business value scores. The goal is to deploy
 * the combination of services that maximises total value within the memory budget.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== 0/1 Knapsack — Cloud Service Deployment ===\n");

        List<Knapsack.Item> services = List.of(
            new Knapsack.Item("auth-service",       2, 60),
            new Knapsack.Item("payment-gateway",    5, 90),
            new Knapsack.Item("recommendation-api", 4, 70),
            new Knapsack.Item("analytics-pipeline", 3, 50),
            new Knapsack.Item("notification-svc",   1, 30),
            new Knapsack.Item("search-engine",      6, 100),
            new Knapsack.Item("report-generator",   4, 65),
            new Knapsack.Item("ml-inference",       7, 120),
            new Knapsack.Item("image-processor",    3, 55)
        );

        int capacity = 10; // GB RAM

        System.out.println("Available services:");
        System.out.printf("  %-25s  %6s  %5s%n", "Service", "RAM(GB)", "Value");
        System.out.println("  " + "-".repeat(40));
        services.forEach(s -> System.out.printf("  %-25s  %6d  %5d%n",
            s.name(), s.weight(), s.value()));

        System.out.println("\nRAM budget: " + capacity + " GB\n");

        Knapsack.Solution solution = Knapsack.solve(services, capacity);

        System.out.println("Optimal deployment:");
        int totalRam = 0;
        for (Knapsack.Item item : solution.chosen()) {
            System.out.printf("  %-25s  %6d GB  value %d%n",
                item.name(), item.weight(), item.value());
            totalRam += item.weight();
        }
        System.out.printf("%n  Total RAM used: %d/%d GB%n", totalRam, capacity);
        System.out.printf("  Total value:    %d%n", solution.totalValue());

        // Compare maxValue (O(W) space) with full solution value
        int fastValue = Knapsack.maxValue(services, capacity);
        System.out.printf("%n  Verified by maxValue(): %d (match: %b)%n",
            fastValue, fastValue == solution.totalValue());
    }
}
