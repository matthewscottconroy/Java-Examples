package com.algorithms.dp.foundations;

/**
 * Foundational dynamic programming examples showing the two DP styles
 * (top-down memoization and bottom-up tabulation) on the same problems.
 *
 * <p>DP applies when a problem has:
 * <ul>
 *   <li><b>Optimal substructure</b> — the optimal solution contains optimal solutions
 *       to sub-problems.</li>
 *   <li><b>Overlapping sub-problems</b> — the same sub-problem is solved many times in
 *       a naive recursive solution.</li>
 * </ul>
 */
public final class DPExamples {

    private DPExamples() {}

    // ─── Fibonacci ────────────────────────────────────────────────────────────

    /** Fibonacci: top-down with memoization. */
    public static long fibMemo(int n) {
        long[] memo = new long[Math.max(n + 1, 2)];
        memo[0] = 0; memo[1] = 1;
        return fibHelper(n, memo);
    }

    private static long fibHelper(int n, long[] memo) {
        if (n <= 1) return memo[n];
        if (memo[n] != 0) return memo[n];
        memo[n] = fibHelper(n - 1, memo) + fibHelper(n - 2, memo);
        return memo[n];
    }

    /** Fibonacci: bottom-up tabulation. O(n) time, O(1) space. */
    public static long fibTab(int n) {
        if (n <= 1) return n;
        long prev2 = 0, prev1 = 1;
        for (int i = 2; i <= n; i++) {
            long curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // ─── Coin Change (minimum coins) ─────────────────────────────────────────

    /**
     * Returns the minimum number of coins needed to make {@code amount},
     * or -1 if it is impossible. Coins may be reused.
     * Bottom-up DP.
     */
    public static int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        java.util.Arrays.fill(dp, amount + 1);  // sentinel: larger than any valid answer
        dp[0] = 0;
        for (int a = 1; a <= amount; a++) {
            for (int coin : coins) {
                if (coin <= a) dp[a] = Math.min(dp[a], dp[a - coin] + 1);
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    // ─── Longest Increasing Subsequence ──────────────────────────────────────

    /**
     * Returns the length of the longest strictly increasing subsequence.
     * O(n²) DP.
     */
    public static int lis(int[] arr) {
        if (arr.length == 0) return 0;
        int n = arr.length;
        int[] dp = new int[n];
        java.util.Arrays.fill(dp, 1);
        int best = 1;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (arr[j] < arr[i]) dp[i] = Math.max(dp[i], dp[j] + 1);
            }
            best = Math.max(best, dp[i]);
        }
        return best;
    }

    // ─── Unique Paths (grid DP) ───────────────────────────────────────────────

    /**
     * Returns the number of unique paths from the top-left to the bottom-right
     * of an m×n grid, moving only right or down.
     */
    public static long uniquePaths(int m, int n) {
        long[] dp = new long[n];
        java.util.Arrays.fill(dp, 1);
        for (int row = 1; row < m; row++) {
            for (int col = 1; col < n; col++) {
                dp[col] += dp[col - 1];
            }
        }
        return dp[n - 1];
    }
}
