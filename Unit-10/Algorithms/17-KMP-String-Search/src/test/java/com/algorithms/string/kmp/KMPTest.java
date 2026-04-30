package com.algorithms.string.kmp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KMPTest {

    @Test
    @DisplayName("single occurrence in the middle")
    void singleOccurrence() {
        List<Integer> result = KMP.search("hello world", "world");
        assertEquals(List.of(6), result);
    }

    @Test
    @DisplayName("multiple non-overlapping occurrences")
    void multipleOccurrences() {
        List<Integer> result = KMP.search("abababab", "abab");
        assertEquals(List.of(0, 2, 4), result);
    }

    @Test
    @DisplayName("overlapping pattern occurrences")
    void overlapping() {
        List<Integer> result = KMP.search("aaaa", "aa");
        assertEquals(List.of(0, 1, 2), result);
    }

    @Test
    @DisplayName("pattern not in text returns empty list")
    void notFound() {
        assertTrue(KMP.search("hello", "xyz").isEmpty());
    }

    @Test
    @DisplayName("pattern equals text")
    void patternEqualsText() {
        assertEquals(List.of(0), KMP.search("abc", "abc"));
    }

    @Test
    @DisplayName("pattern longer than text returns empty list")
    void patternLongerThanText() {
        assertTrue(KMP.search("ab", "abc").isEmpty());
    }

    @Test
    @DisplayName("empty pattern returns empty list")
    void emptyPattern() {
        assertTrue(KMP.search("hello", "").isEmpty());
    }

    @Test
    @DisplayName("pattern at start and end")
    void startAndEnd() {
        List<Integer> result = KMP.search("abcXYZabc", "abc");
        assertEquals(List.of(0, 6), result);
    }

    @Test
    @DisplayName("failure function: AABAAB")
    void failureFunctionAABAAB() {
        int[] fail = KMP.buildFailureFunction("AABAAB");
        assertArrayEquals(new int[]{0, 1, 0, 1, 2, 3}, fail);
    }

    @Test
    @DisplayName("failure function: all same characters")
    void failureFunctionAllSame() {
        int[] fail = KMP.buildFailureFunction("AAAA");
        assertArrayEquals(new int[]{0, 1, 2, 3}, fail);
    }

    @Test
    @DisplayName("failure function: no repeating prefix")
    void failureFunctionNoRepeat() {
        int[] fail = KMP.buildFailureFunction("ABCDE");
        assertArrayEquals(new int[]{0, 0, 0, 0, 0}, fail);
    }

    @Test
    @DisplayName("contains returns true when pattern present")
    void containsTrue() {
        assertTrue(KMP.contains("the quick brown fox", "quick"));
    }

    @Test
    @DisplayName("contains returns false when pattern absent")
    void containsFalse() {
        assertFalse(KMP.contains("the quick brown fox", "slow"));
    }

    @Test
    @DisplayName("agrees with String.indexOf on 10000 random strings")
    void agreesWithIndexOf() {
        String text = "abcabcabdabcabcabc".repeat(500);
        String pattern = "abcabcabc";
        List<Integer> kmpResult = KMP.search(text, pattern);

        // Count with indexOf
        int count = 0;
        int i = 0;
        while ((i = text.indexOf(pattern, i)) != -1) { count++; i++; }
        assertEquals(count, kmpResult.size());
    }
}
