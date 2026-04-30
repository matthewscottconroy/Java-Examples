package com.algorithms.dp.foundations;

/**
 * Demonstrates dynamic programming foundations: memoization vs tabulation,
 * coin change, longest increasing subsequence, and grid paths.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Dynamic Programming Foundations ===\n");

        // --- Fibonacci ---
        System.out.println("--- Fibonacci: Memoization vs Tabulation ---");
        System.out.println("  n    fib(n)");
        for (int n : new int[]{0, 1, 5, 10, 20, 40, 50}) {
            long memo = DPExamples.fibMemo(n);
            long tab  = DPExamples.fibTab(n);
            System.out.printf("  %-4d %-20d (memo=%d, tab=%d, match=%b)%n",
                n, memo, memo, tab, memo == tab);
        }

        // --- Coin Change ---
        System.out.println("\n--- Coin Change: Minimum Coins ---");
        int[][] scenarios = {
            {11, 1, 5, 6, 9},  // 11 with coins [1,5,6,9] → 2 (5+6)
            {11, 1, 5, 6},     // 11 with coins [1,5,6]   → 3 (5+6 no, 1+1+... = 3 coins: 6+5=11 nope, 6+4=10... actually 6+5=11 yes)
            {3,  2},           // 3 with coins [2]         → impossible
            {0,  1, 5},        // amount=0 → 0 coins
        };
        for (int[] s : scenarios) {
            int amount = s[0];
            int[] coins = new int[s.length - 1];
            for (int i = 1; i < s.length; i++) coins[i - 1] = s[i];
            int result = DPExamples.coinChange(coins, amount);
            System.out.printf("  amount=%2d, coins=%s → %s%n",
                amount, java.util.Arrays.toString(coins),
                result == -1 ? "impossible" : result + " coins");
        }

        // --- Longest Increasing Subsequence ---
        System.out.println("\n--- Longest Increasing Subsequence ---");
        int[][] seqs = {
            {10, 9, 2, 5, 3, 7, 101, 18},   // → 4 (2,3,7,18 or 2,5,7,18)
            {0, 1, 0, 3, 2, 3},              // → 4
            {7, 7, 7, 7},                    // → 1
            {1, 2, 3, 4, 5},                 // → 5
        };
        for (int[] seq : seqs) {
            System.out.printf("  %s → LIS length %d%n",
                java.util.Arrays.toString(seq), DPExamples.lis(seq));
        }

        // --- Unique Paths ---
        System.out.println("\n--- Unique Paths (Robot on Grid) ---");
        System.out.println("  A robot starts top-left, can only move right or down.");
        int[][] grids = {{2,2},{3,3},{3,7},{10,10}};
        for (int[] g : grids) {
            System.out.printf("  %d×%d grid → %d unique paths%n",
                g[0], g[1], DPExamples.uniquePaths(g[0], g[1]));
        }
    }
}
