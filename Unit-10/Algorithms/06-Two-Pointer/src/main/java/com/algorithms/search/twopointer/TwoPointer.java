package com.algorithms.search.twopointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Two-pointer and sliding-window algorithms on arrays and lists.
 *
 * <p>Two-pointer: maintain left and right indices that move toward each other
 * (or in the same direction) to solve problems in O(n) that naively take O(n²).
 *
 * <p>Sliding window: maintain a window [lo, hi) that expands right and shrinks
 * left to track a property (sum, count, distinct elements) over a subarray.
 */
public final class TwoPointer {

    private TwoPointer() {}

    /**
     * Finds a pair of indices [i, j] in a sorted array such that
     * {@code arr[i] + arr[j] == target}, or empty if none exists.
     * Array must be sorted ascending.
     */
    public static Optional<int[]> twoSum(int[] arr, int target) {
        int lo = 0, hi = arr.length - 1;
        while (lo < hi) {
            int sum = arr[lo] + arr[hi];
            if      (sum == target) return Optional.of(new int[]{lo, hi});
            else if (sum <  target) lo++;
            else                    hi--;
        }
        return Optional.empty();
    }

    /**
     * Returns the length of the longest contiguous subarray whose sum ≤ maxSum.
     * All elements must be non-negative.
     */
    public static int longestSubarrayWithSumAtMost(int[] arr, int maxSum) {
        int lo = 0, currentSum = 0, best = 0;
        for (int hi = 0; hi < arr.length; hi++) {
            currentSum += arr[hi];
            while (currentSum > maxSum) currentSum -= arr[lo++];
            best = Math.max(best, hi - lo + 1);
        }
        return best;
    }

    /**
     * Returns all unique triplets [a, b, c] from the sorted array such that
     * a + b + c == 0.
     */
    public static List<int[]> threeSum(int[] sortedArr) {
        List<int[]> result = new ArrayList<>();
        int n = sortedArr.length;
        for (int i = 0; i < n - 2; i++) {
            if (i > 0 && sortedArr[i] == sortedArr[i - 1]) continue;  // skip duplicates
            int lo = i + 1, hi = n - 1;
            while (lo < hi) {
                int sum = sortedArr[i] + sortedArr[lo] + sortedArr[hi];
                if (sum == 0) {
                    result.add(new int[]{sortedArr[i], sortedArr[lo], sortedArr[hi]});
                    while (lo < hi && sortedArr[lo] == sortedArr[lo + 1]) lo++;
                    while (lo < hi && sortedArr[hi] == sortedArr[hi - 1]) hi--;
                    lo++; hi--;
                } else if (sum < 0) lo++;
                else                hi--;
            }
        }
        return result;
    }

    /**
     * Returns the minimum length of a contiguous subarray whose sum ≥ target.
     * Returns 0 if no such subarray exists. All elements must be positive.
     */
    public static int minSubarrayLength(int[] arr, int target) {
        int lo = 0, currentSum = 0, best = Integer.MAX_VALUE;
        for (int hi = 0; hi < arr.length; hi++) {
            currentSum += arr[hi];
            while (currentSum >= target) {
                best = Math.min(best, hi - lo + 1);
                currentSum -= arr[lo++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }
}
