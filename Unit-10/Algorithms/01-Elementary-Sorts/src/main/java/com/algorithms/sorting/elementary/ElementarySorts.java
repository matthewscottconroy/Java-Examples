package com.algorithms.sorting.elementary;

/**
 * Three O(n²) sorting algorithms, each instrumented to count comparisons and swaps.
 *
 * <p>The three algorithms are behaviourally identical in that they all produce a
 * sorted result, but they differ in <em>how</em> they move elements and therefore
 * in their constant factors and best-case behaviour.
 */
public final class ElementarySorts {

    private ElementarySorts() {}

    /**
     * Bubble sort — repeatedly walk the array, swapping adjacent elements that
     * are out of order. Each pass "bubbles" the largest unsorted element to its
     * final position.
     *
     * <p>Best case O(n) when the array is already sorted (with early exit).
     * Average and worst case O(n²).
     */
    public static <T extends Comparable<T>> SortStats bubbleSort(T[] arr) {
        SortStats stats = new SortStats("Bubble");
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                stats.comparison();
                if (arr[j].compareTo(arr[j + 1]) > 0) {
                    swap(arr, j, j + 1);
                    stats.swap();
                    swapped = true;
                }
            }
            if (!swapped) break; // already sorted
        }
        return stats;
    }

    /**
     * Selection sort — find the minimum of the unsorted portion and place it
     * at the start, shrinking the unsorted portion by one each pass.
     *
     * <p>Always O(n²) comparisons regardless of input order.
     * Performs at most O(n) swaps — useful when writes are expensive.
     */
    public static <T extends Comparable<T>> SortStats selectionSort(T[] arr) {
        SortStats stats = new SortStats("Selection");
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                stats.comparison();
                if (arr[j].compareTo(arr[minIdx]) < 0) minIdx = j;
            }
            if (minIdx != i) { swap(arr, i, minIdx); stats.swap(); }
        }
        return stats;
    }

    /**
     * Insertion sort — take each element and insert it into the correct position
     * among the already-sorted prefix, shifting larger elements right.
     *
     * <p>Best case O(n) on nearly-sorted data.
     * Works well on small arrays and is the inner loop of many hybrid sorts.
     */
    public static <T extends Comparable<T>> SortStats insertionSort(T[] arr) {
        SortStats stats = new SortStats("Insertion");
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            T key = arr[i];
            int j = i - 1;
            while (j >= 0) {
                stats.comparison();
                if (arr[j].compareTo(key) > 0) {
                    arr[j + 1] = arr[j];
                    stats.swap();
                    j--;
                } else break;
            }
            arr[j + 1] = key;
        }
        return stats;
    }

    private static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
    }
}
