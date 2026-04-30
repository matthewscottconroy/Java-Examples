package com.algorithms.dp.lcs;

/**
 * Longest Common Subsequence (LCS) and related edit-distance operations.
 *
 * <p>A subsequence is a sequence derived by deleting zero or more elements without
 * changing the relative order. "ACE" is a subsequence of "ABCDE"; "AEC" is not.
 *
 * <p>LCS is the foundation of diff tools, DNA sequence alignment, and spell-checkers.
 *
 * <p>Time: O(m·n).  Space: O(m·n) for the full table; O(min(m,n)) for length only.
 */
public final class LCS {

    private LCS() {}

    /**
     * Returns the length of the longest common subsequence of {@code a} and {@code b}.
     */
    public static int length(String a, String b) {
        int m = a.length(), n = b.length();
        // Use two rolling rows to reduce space to O(n)
        int[] prev = new int[n + 1], curr = new int[n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1))
                    curr[j] = prev[j - 1] + 1;
                else
                    curr[j] = Math.max(prev[j], curr[j - 1]);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
            java.util.Arrays.fill(curr, 0);
        }
        return prev[n];
    }

    /**
     * Returns the actual LCS string by reconstructing from the full DP table.
     */
    public static String reconstruct(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                    ? dp[i - 1][j - 1] + 1
                    : Math.max(dp[i - 1][j], dp[i][j - 1]);

        StringBuilder sb = new StringBuilder();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sb.append(a.charAt(i - 1));
                i--; j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return sb.reverse().toString();
    }

    /**
     * Returns the edit distance (Levenshtein) between {@code a} and {@code b}:
     * the minimum number of single-character insertions, deletions, or substitutions.
     */
    public static int editDistance(String a, String b) {
        int m = a.length(), n = b.length();
        int[] prev = new int[n + 1], curr = new int[n + 1];
        for (int j = 0; j <= n; j++) prev[j] = j;
        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1))
                    curr[j] = prev[j - 1];
                else
                    curr[j] = 1 + Math.min(prev[j - 1], Math.min(prev[j], curr[j - 1]));
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[n];
    }
}
