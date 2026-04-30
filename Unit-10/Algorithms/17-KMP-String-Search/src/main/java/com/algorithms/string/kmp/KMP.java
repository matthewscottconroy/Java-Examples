package com.algorithms.string.kmp;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt (KMP) string search algorithm.
 *
 * <p>Finds all occurrences of a pattern in a text in O(n + m) time,
 * where n = text length and m = pattern length. The key is the
 * "failure function" (prefix table): when a mismatch occurs, it tells
 * us the longest proper prefix of the matched portion that is also a
 * suffix, so we can skip ahead without re-examining characters.
 *
 * <p>Contrast with naïve O(n·m) search which re-checks characters after
 * a partial match fails.
 */
public final class KMP {

    private KMP() {}

    /**
     * Returns all starting indices where {@code pattern} occurs in {@code text}.
     * Indices are 0-based.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty()) return matches;
        int n = text.length(), m = pattern.length();
        int[] fail = buildFailureFunction(pattern);

        int j = 0;  // number of characters in pattern matched so far
        for (int i = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) j = fail[j - 1];
            if (text.charAt(i) == pattern.charAt(j)) j++;
            if (j == m) {
                matches.add(i - m + 1);
                j = fail[j - 1];  // look for overlapping matches
            }
        }
        return matches;
    }

    /**
     * Returns the failure function (partial match table) for a pattern.
     *
     * <p>{@code fail[i]} = length of the longest proper prefix of
     * {@code pattern[0..i]} that is also a suffix.
     */
    public static int[] buildFailureFunction(String pattern) {
        int m = pattern.length();
        int[] fail = new int[m];
        fail[0] = 0;
        int k = 0;
        for (int i = 1; i < m; i++) {
            while (k > 0 && pattern.charAt(i) != pattern.charAt(k)) k = fail[k - 1];
            if (pattern.charAt(i) == pattern.charAt(k)) k++;
            fail[i] = k;
        }
        return fail;
    }

    /**
     * Returns true if {@code pattern} occurs at least once in {@code text}.
     */
    public static boolean contains(String text, String pattern) {
        return !search(text, pattern).isEmpty();
    }
}
