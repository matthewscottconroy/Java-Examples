package com.algorithms.dp.knapsack;

import java.util.ArrayList;
import java.util.List;

/**
 * 0/1 Knapsack: choose items to maximise value without exceeding capacity.
 *
 * <p>Each item can be included at most once (0/1). If items can be included
 * multiple times, that's the "unbounded knapsack" variant.
 *
 * <p>DP recurrence:
 * <pre>
 * dp[i][w] = max value using the first i items with capacity w
 *          = max(dp[i-1][w],                       // skip item i
 *                dp[i-1][w - weight[i]] + value[i]) // include item i (if weight[i] ≤ w)
 * </pre>
 *
 * <p>Time: O(n·W).  Space: O(W) with rolling-row optimization.
 */
public final class Knapsack {

    private Knapsack() {}

    public record Item(String name, int weight, int value) {}

    public record Solution(int totalValue, List<Item> chosen) {}

    /**
     * Solves the 0/1 knapsack problem and returns the optimal total value
     * along with the selected items.
     */
    public static Solution solve(List<Item> items, int capacity) {
        int n = items.size();
        // Full table needed for reconstruction
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            Item item = items.get(i - 1);
            for (int w = 0; w <= capacity; w++) {
                dp[i][w] = dp[i - 1][w];
                if (item.weight() <= w) {
                    dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - item.weight()] + item.value());
                }
            }
        }

        // Reconstruct which items were chosen
        List<Item> chosen = new ArrayList<>();
        int w = capacity;
        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                chosen.add(0, items.get(i - 1));
                w -= items.get(i - 1).weight();
            }
        }
        return new Solution(dp[n][capacity], chosen);
    }

    /**
     * Returns only the maximum achievable value (O(W) space, no reconstruction).
     */
    public static int maxValue(List<Item> items, int capacity) {
        int[] dp = new int[capacity + 1];
        for (Item item : items) {
            for (int w = capacity; w >= item.weight(); w--) {
                dp[w] = Math.max(dp[w], dp[w - item.weight()] + item.value());
            }
        }
        return dp[capacity];
    }
}
