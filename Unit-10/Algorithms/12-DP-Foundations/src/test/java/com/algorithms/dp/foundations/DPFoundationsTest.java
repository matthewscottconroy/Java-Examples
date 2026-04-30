package com.algorithms.dp.foundations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DPFoundationsTest {

    @Test
    @DisplayName("fibMemo and fibTab agree on first 20 values")
    void fibAgreement() {
        for (int n = 0; n <= 20; n++) {
            assertEquals(DPExamples.fibMemo(n), DPExamples.fibTab(n),
                "Mismatch at n=" + n);
        }
    }

    @Test
    @DisplayName("fib(0)=0, fib(1)=1, fib(10)=55")
    void fibKnownValues() {
        assertEquals(0L,  DPExamples.fibTab(0));
        assertEquals(1L,  DPExamples.fibTab(1));
        assertEquals(55L, DPExamples.fibTab(10));
    }

    @Test
    @DisplayName("coinChange: 5+6=11 is 2 coins with [1,5,6,9]")
    void coinChangeBasic() {
        assertEquals(2, DPExamples.coinChange(new int[]{1, 5, 6, 9}, 11));
    }

    @Test
    @DisplayName("coinChange: 3 coins needed for 11 with [1,5]")
    void coinChangeThreeCoins() {
        // 5+5+1=11 → 3 coins
        assertEquals(3, DPExamples.coinChange(new int[]{1, 5}, 11));
    }

    @Test
    @DisplayName("coinChange returns -1 when impossible")
    void coinChangeImpossible() {
        assertEquals(-1, DPExamples.coinChange(new int[]{2}, 3));
    }

    @Test
    @DisplayName("coinChange(0) = 0 coins")
    void coinChangeZero() {
        assertEquals(0, DPExamples.coinChange(new int[]{1, 5}, 0));
    }

    @Test
    @DisplayName("LIS of strictly increasing sequence equals length")
    void lisIncreasing() {
        assertEquals(5, DPExamples.lis(new int[]{1, 2, 3, 4, 5}));
    }

    @Test
    @DisplayName("LIS of all equal elements is 1")
    void lisAllEqual() {
        assertEquals(1, DPExamples.lis(new int[]{7, 7, 7, 7}));
    }

    @Test
    @DisplayName("LIS classic example: [10,9,2,5,3,7,101,18] = 4")
    void lisClassic() {
        assertEquals(4, DPExamples.lis(new int[]{10, 9, 2, 5, 3, 7, 101, 18}));
    }

    @Test
    @DisplayName("LIS of empty array is 0")
    void lisEmpty() {
        assertEquals(0, DPExamples.lis(new int[]{}));
    }

    @Test
    @DisplayName("uniquePaths: 1×n grid has exactly 1 path")
    void uniquePathsSingleRow() {
        assertEquals(1L, DPExamples.uniquePaths(1, 10));
    }

    @Test
    @DisplayName("uniquePaths: 2×2 grid has 2 paths")
    void uniquePaths2x2() {
        assertEquals(2L, DPExamples.uniquePaths(2, 2));
    }

    @Test
    @DisplayName("uniquePaths: 3×3 grid has 6 paths")
    void uniquePaths3x3() {
        assertEquals(6L, DPExamples.uniquePaths(3, 3));
    }

    @Test
    @DisplayName("uniquePaths: 3×7 grid has 28 paths")
    void uniquePaths3x7() {
        assertEquals(28L, DPExamples.uniquePaths(3, 7));
    }
}
