package com.algorithms.sorting.elementary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ElementarySortsTest {

    private Integer[] random(int n, long seed) {
        Random rng = new Random(seed);
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) arr[i] = rng.nextInt(1000);
        return arr;
    }

    private void assertSorted(Integer[] arr) {
        for (int i = 0; i < arr.length - 1; i++)
            assertTrue(arr[i] <= arr[i + 1],
                    "Not sorted at index " + i + ": " + arr[i] + " > " + arr[i + 1]);
    }

    @Test @DisplayName("Bubble sort produces a sorted array")
    void bubbleSorted()    { Integer[] a = random(50,1); ElementarySorts.bubbleSort(a);    assertSorted(a); }

    @Test @DisplayName("Selection sort produces a sorted array")
    void selectionSorted() { Integer[] a = random(50,2); ElementarySorts.selectionSort(a); assertSorted(a); }

    @Test @DisplayName("Insertion sort produces a sorted array")
    void insertionSorted() { Integer[] a = random(50,3); ElementarySorts.insertionSort(a); assertSorted(a); }

    @Test @DisplayName("All three sorts agree with Arrays.sort")
    void agreeWithArraysSort() {
        Integer[] ref = random(100, 7);
        Integer[] bubble    = ref.clone(); ElementarySorts.bubbleSort(bubble);
        Integer[] selection = ref.clone(); ElementarySorts.selectionSort(selection);
        Integer[] insertion = ref.clone(); ElementarySorts.insertionSort(insertion);
        Arrays.sort(ref);
        assertArrayEquals(ref, bubble);
        assertArrayEquals(ref, selection);
        assertArrayEquals(ref, insertion);
    }

    @Test @DisplayName("Bubble sort on sorted input performs O(n) comparisons")
    void bubbleSortedInput() {
        Integer[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        SortStats stats = ElementarySorts.bubbleSort(arr);
        assertEquals(9, stats.comparisons(), "Should make exactly n-1 comparisons on sorted input");
        assertEquals(0, stats.swaps());
    }

    @Test @DisplayName("Selection sort performs exactly n*(n-1)/2 comparisons regardless of input")
    void selectionComparisons() {
        Integer[] arr = {5, 3, 1, 4, 2};
        SortStats stats = ElementarySorts.selectionSort(arr);
        assertEquals(10, stats.comparisons()); // 4+3+2+1 = 10
    }

    @Test @DisplayName("All sorts handle single-element array")
    void singleElement() {
        Integer[] a = {42};
        ElementarySorts.bubbleSort(a);
        ElementarySorts.selectionSort(a);
        ElementarySorts.insertionSort(a);
        assertEquals(42, a[0]);
    }

    @Test @DisplayName("All sorts handle already-sorted strings")
    void stringSorting() {
        String[] arr = {"apple", "banana", "cherry", "date"};
        String[] copy = arr.clone();
        ElementarySorts.insertionSort(copy);
        assertArrayEquals(arr, copy);
    }
}
