package com.algorithms.search.twopointer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwoPointerTest {

    @Test
    @DisplayName("twoSum finds a valid pair")
    void twoSumFound() {
        int[] arr = {1, 3, 5, 7, 9, 11};
        var result = TwoPointer.twoSum(arr, 12);
        assertTrue(result.isPresent());
        int[] p = result.get();
        assertEquals(12, arr[p[0]] + arr[p[1]]);
        assertTrue(p[0] < p[1]);
    }

    @Test
    @DisplayName("twoSum returns empty when no pair exists")
    void twoSumNotFound() {
        int[] arr = {1, 3, 5, 7, 9};
        assertTrue(TwoPointer.twoSum(arr, 2).isEmpty());
    }

    @Test
    @DisplayName("twoSum works with negative numbers")
    void twoSumNegative() {
        int[] arr = {-5, -3, -1, 2, 4, 6};
        var result = TwoPointer.twoSum(arr, 1);
        assertTrue(result.isPresent());
        int[] p = result.get();
        assertEquals(1, arr[p[0]] + arr[p[1]]);
    }

    @Test
    @DisplayName("longestSubarrayWithSumAtMost returns correct window length")
    void longestWindow() {
        // [1,2,3]: sum=6 ≤ 6, length=3
        int[] arr = {1, 2, 3, 4, 5};
        assertEquals(3, TwoPointer.longestSubarrayWithSumAtMost(arr, 6));
    }

    @Test
    @DisplayName("longestSubarrayWithSumAtMost returns 0 for empty array")
    void longestWindowEmpty() {
        assertEquals(0, TwoPointer.longestSubarrayWithSumAtMost(new int[]{}, 10));
    }

    @Test
    @DisplayName("longestSubarrayWithSumAtMost entire array when all fit")
    void longestWindowAllFit() {
        int[] arr = {1, 2, 3};
        assertEquals(3, TwoPointer.longestSubarrayWithSumAtMost(arr, 100));
    }

    @Test
    @DisplayName("threeSum finds all zero-sum triplets")
    void threeSumBasic() {
        int[] arr = {-4, -1, -1, 0, 1, 2};
        List<int[]> result = TwoPointer.threeSum(arr);
        assertFalse(result.isEmpty());
        // every triplet should sum to 0
        result.forEach(t -> assertEquals(0, t[0] + t[1] + t[2]));
    }

    @Test
    @DisplayName("threeSum deduplicates when multiple equal triplets exist")
    void threeSumNoDuplicates() {
        int[] arr = {-1, -1, 0, 0, 1, 1};
        List<int[]> result = TwoPointer.threeSum(arr);
        // only unique triplets: [-1, 0, 1]
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("threeSum returns empty when no zero-sum triplet exists")
    void threeSumNone() {
        int[] arr = {1, 2, 3, 4, 5};
        assertTrue(TwoPointer.threeSum(arr).isEmpty());
    }

    @Test
    @DisplayName("minSubarrayLength finds minimum window")
    void minSubarray() {
        int[] arr = {2, 3, 1, 2, 4, 3};
        assertEquals(2, TwoPointer.minSubarrayLength(arr, 7));
    }

    @Test
    @DisplayName("minSubarrayLength returns 0 when target unreachable")
    void minSubarrayImpossible() {
        int[] arr = {1, 2, 3};
        assertEquals(0, TwoPointer.minSubarrayLength(arr, 100));
    }

    @Test
    @DisplayName("minSubarrayLength returns 1 when single element meets target")
    void minSubarraySingle() {
        int[] arr = {1, 5, 3};
        assertEquals(1, TwoPointer.minSubarrayLength(arr, 5));
    }
}
