package com.generics.bounded;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmsTest {

    // -- min --

    @Test @DisplayName("min returns smallest integer")
    void min_integers() {
        assertEquals(1, Algorithms.min(List.of(3, 1, 4, 1, 5)));
    }

    @Test @DisplayName("min returns smallest string lexicographically")
    void min_strings() {
        assertEquals("apple", Algorithms.min(List.of("orange", "apple", "mango")));
    }

    @Test @DisplayName("min of single-element list returns that element")
    void min_singleton() {
        assertEquals(7, Algorithms.min(List.of(7)));
    }

    @Test @DisplayName("min throws for empty list")
    void min_empty() {
        List<Integer> empty = List.of();
        assertThrows(IllegalArgumentException.class, () -> Algorithms.min(empty));
    }

    // -- max --

    @Test @DisplayName("max returns largest integer")
    void max_integers() {
        assertEquals(9, Algorithms.max(List.of(3, 9, 1, 4)));
    }

    @Test @DisplayName("max returns last string lexicographically")
    void max_strings() {
        assertEquals("zebra", Algorithms.max(List.of("ant", "zebra", "mango")));
    }

    @Test @DisplayName("max throws for empty list")
    void max_empty() {
        List<Integer> empty = List.of();
        assertThrows(IllegalArgumentException.class, () -> Algorithms.max(empty));
    }

    // -- binarySearch --

    @Test @DisplayName("binarySearch finds an element in a sorted list")
    void binarySearch_found() {
        List<Integer> sorted = List.of(1, 3, 5, 7, 9);
        assertEquals(2, Algorithms.binarySearch(sorted, 5));
    }

    @Test @DisplayName("binarySearch finds the first element")
    void binarySearch_first() {
        List<Integer> sorted = List.of(2, 4, 6);
        assertEquals(0, Algorithms.binarySearch(sorted, 2));
    }

    @Test @DisplayName("binarySearch finds the last element")
    void binarySearch_last() {
        List<Integer> sorted = List.of(2, 4, 6);
        assertEquals(2, Algorithms.binarySearch(sorted, 6));
    }

    @Test @DisplayName("binarySearch returns negative value when element is absent")
    void binarySearch_notFound() {
        List<Integer> sorted = List.of(1, 3, 5, 7);
        assertTrue(Algorithms.binarySearch(sorted, 4) < 0);
    }

    @Test @DisplayName("binarySearch works on strings")
    void binarySearch_strings() {
        List<String> sorted = List.of("cat", "dog", "fox", "gnu");
        assertEquals(1, Algorithms.binarySearch(sorted, "dog"));
    }
}
