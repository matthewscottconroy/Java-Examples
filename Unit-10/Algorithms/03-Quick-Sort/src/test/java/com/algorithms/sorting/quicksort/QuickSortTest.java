package com.algorithms.sorting.quicksort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class QuickSortTest {

    @Test @DisplayName("Sorts random integers")
    void sortsRandom() {
        Integer[] arr = {5,2,8,1,9,3,7,4,6};
        QuickSort.sort(arr);
        assertArrayEquals(new Integer[]{1,2,3,4,5,6,7,8,9}, arr);
    }

    @Test @DisplayName("Agrees with Arrays.sort for n=5000")
    void agreesWithArraysSort() {
        Random rng = new Random(77);
        Integer[] arr = new Integer[5000];
        for (int i = 0; i < arr.length; i++) arr[i] = rng.nextInt(10000);
        Integer[] expected = arr.clone();
        Arrays.sort(expected);
        QuickSort.sort(arr);
        assertArrayEquals(expected, arr);
    }

    @Test @DisplayName("Handles all-duplicate array")
    void allDuplicates() {
        Integer[] arr = {5,5,5,5,5};
        QuickSort.sort(arr);
        assertArrayEquals(new Integer[]{5,5,5,5,5}, arr);
    }

    @Test @DisplayName("Handles already-sorted input")
    void alreadySorted() {
        Integer[] arr = {1,2,3,4,5};
        QuickSort.sort(arr);
        assertArrayEquals(new Integer[]{1,2,3,4,5}, arr);
    }

    @Test @DisplayName("Handles reverse-sorted input")
    void reverseSorted() {
        Integer[] arr = {5,4,3,2,1};
        QuickSort.sort(arr);
        assertArrayEquals(new Integer[]{1,2,3,4,5}, arr);
    }

    @Test @DisplayName("Custom comparator sorts descending")
    void customComparator() {
        Integer[] arr = {3,1,4,1,5,9,2,6};
        QuickSort.sort(arr, Comparator.reverseOrder());
        assertArrayEquals(new Integer[]{9,6,5,4,3,2,1,1}, arr);
    }

    @Test @DisplayName("Single element remains unchanged")
    void singleElement() {
        Integer[] arr = {42};
        QuickSort.sort(arr);
        assertEquals(42, arr[0]);
    }
}
