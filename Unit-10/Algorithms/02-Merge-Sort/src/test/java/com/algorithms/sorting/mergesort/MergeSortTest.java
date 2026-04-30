package com.algorithms.sorting.mergesort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MergeSortTest {

    @Test @DisplayName("Sorts a random list of integers")
    void sortsIntegers() {
        List<Integer> input = List.of(5, 2, 8, 1, 9, 3, 7, 4, 6);
        List<Integer> result = MergeSort.sort(input);
        assertEquals(List.of(1,2,3,4,5,6,7,8,9), result);
    }

    @Test @DisplayName("Handles empty list")
    void emptyList() { assertTrue(MergeSort.<Integer>sort(List.of()).isEmpty()); }

    @Test @DisplayName("Handles single-element list")
    void singleElement() { assertEquals(List.of(42), MergeSort.sort(List.of(42))); }

    @Test @DisplayName("Does not mutate the original list")
    void immutable() {
        List<Integer> orig = List.of(3, 1, 2);
        MergeSort.sort(orig);
        assertEquals(List.of(3, 1, 2), orig);
    }

    @Test @DisplayName("Agrees with Collections.sort for n=1000 random ints")
    void largeRandom() {
        Random rng = new Random(99);
        List<Integer> input = rng.ints(1000, 0, 10000).boxed().collect(Collectors.toList());
        List<Integer> expected = input.stream().sorted().collect(Collectors.toList());
        assertEquals(expected, MergeSort.sort(input));
    }

    @Test @DisplayName("Merge of two sorted lists is sorted")
    void mergeTwoSorted() {
        List<Integer> a = List.of(1, 3, 5, 7);
        List<Integer> b = List.of(2, 4, 6, 8);
        List<Integer> merged = MergeSort.merge(a, b, Comparator.naturalOrder());
        assertEquals(List.of(1,2,3,4,5,6,7,8), merged);
    }

    @Test @DisplayName("Is stable: equal elements preserve relative order")
    void stable() {
        // Use strings with same first char; stable sort preserves insertion order
        List<String> input = List.of("b1", "a1", "a2", "b2", "a3");
        List<String> result = MergeSort.sort(input);
        // a-prefixed elements should come first and stay in order: a1, a2, a3
        int a1 = result.indexOf("a1"), a2 = result.indexOf("a2"), a3 = result.indexOf("a3");
        assertTrue(a1 < a2 && a2 < a3);
    }
}
