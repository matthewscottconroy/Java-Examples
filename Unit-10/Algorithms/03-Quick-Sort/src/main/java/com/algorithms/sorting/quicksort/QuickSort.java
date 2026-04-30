package com.algorithms.sorting.quicksort;

import java.util.Comparator;
import java.util.Random;

/**
 * In-place quick sort with three-way partitioning and random pivot selection.
 *
 * <p>Three-way partitioning (Dutch National Flag) handles duplicate keys in
 * O(n) per partition instead of O(n²), making it efficient for real-world
 * data that contains repeated values.
 *
 * <p>Random pivot selection eliminates O(n²) worst-case behaviour on
 * sorted or reverse-sorted inputs.
 */
public final class QuickSort {

    private static final Random RNG = new Random(42);

    private QuickSort() {}

    public static <T> void sort(T[] arr, Comparator<T> cmp) {
        sort(arr, cmp, 0, arr.length - 1);
    }

    public static <T extends Comparable<T>> void sort(T[] arr) {
        sort(arr, Comparator.naturalOrder());
    }

    private static <T> void sort(T[] arr, Comparator<T> cmp, int lo, int hi) {
        if (lo >= hi) return;

        // Random pivot: swap a random element into position lo
        int pivotIdx = lo + RNG.nextInt(hi - lo + 1);
        swap(arr, lo, pivotIdx);

        // Three-way partition: arr[lo..lt-1] < pivot, arr[lt..gt] == pivot, arr[gt+1..hi] > pivot
        T pivot = arr[lo];
        int lt = lo, i = lo + 1, gt = hi;
        while (i <= gt) {
            int cmpResult = cmp.compare(arr[i], pivot);
            if      (cmpResult < 0) swap(arr, lt++, i++);
            else if (cmpResult > 0) swap(arr, i, gt--);
            else                    i++;
        }

        sort(arr, cmp, lo, lt - 1);
        sort(arr, cmp, gt + 1, hi);
    }

    private static <T> void swap(T[] arr, int i, int j) {
        T t = arr[i]; arr[i] = arr[j]; arr[j] = t;
    }
}
