package com.algorithms.tree.bst;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates BST on a stock price index.
 *
 * Company tickers are the keys; closing prices are the values.
 * BST provides O(log n) lookup, sorted iteration, and range queries via floor/ceiling.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Binary Search Tree — Stock Price Index ===\n");

        BST<String, Double> index = new BST<>();
        String[][] stocks = {
            {"MSFT", "415.50"}, {"GOOG", "178.30"}, {"AAPL", "189.25"},
            {"AMZN", "198.75"}, {"META", "505.10"}, {"TSLA",  "248.40"},
            {"NVDA", "875.20"}, {"AMD",  "164.80"}, {"INTC",  "43.50"},
            {"IBM",  "197.60"}
        };

        for (String[] s : stocks) index.put(s[0], Double.parseDouble(s[1]));

        // In-order traversal gives sorted tickers
        System.out.println("Tickers in sorted order:");
        List<String> sorted = index.inOrder();
        sorted.forEach(ticker ->
            System.out.printf("  %-6s  $%.2f%n", ticker, index.get(ticker)));

        // Lookup
        System.out.println("\nLookups:");
        System.out.printf("  NVDA → $%.2f%n", index.get("NVDA"));
        System.out.printf("  XYZ  → %s%n",    index.get("XYZ"));

        // Min/Max
        System.out.printf("%nMin ticker: %s ($%.2f)%n",
            index.min().orElse(""), index.get(index.min().orElse("")));
        System.out.printf("Max ticker: %s ($%.2f)%n",
            index.max().orElse(""), index.get(index.max().orElse("")));

        // Floor/Ceiling (range query: "nearest ticker to a given query")
        System.out.println("\nFloor/Ceiling queries:");
        String[] queries = {"APPLE", "GOOG", "ORACLE", "ZOOM"};
        for (String q : queries) {
            System.out.printf("  Query \"%-7s\"  floor: %-6s  ceiling: %s%n",
                q + "\"",
                index.floor(q).orElse("none"),
                index.ceiling(q).orElse("none"));
        }

        // Height analysis
        System.out.println("\n--- BST Height Analysis ---");
        System.out.println("Balanced insertion (" + index.size() + " stocks): height = " + index.height());

        // Compare with sorted insertion (degenerates)
        BST<Integer, String> sorted10 = new BST<>();
        for (int i = 1; i <= 10; i++) sorted10.put(i, "v" + i);
        System.out.println("Sorted insertion (1..10): height = " + sorted10.height()
            + " (degenerate — O(n) worst case)");

        // Random insertion
        Random rng = new Random(42);
        BST<Integer, String> random100 = new BST<>();
        for (int i = 0; i < 100; i++) random100.put(rng.nextInt(10_000), "v" + i);
        System.out.printf("Random insertion (%d nodes, 100 inserts): height = %d%n",
            random100.size(), random100.height());
    }
}
