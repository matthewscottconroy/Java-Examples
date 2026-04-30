package com.algorithms.sorting.heapsort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Heap sort using the MinHeap as the engine.
 *
 * <p>Time: O(n log n) — each insert and extractMin is O(log n), done n times.
 * Space: O(n) — the heap holds all elements.
 *
 * <p>Not in-place (unlike the classic in-array variant), but the intent here is
 * to show the heap as a priority queue rather than as an array trick.
 */
public final class HeapSort {

    private HeapSort() {}

    public static <T extends Comparable<T>> List<T> sort(List<T> input) {
        return sort(input, Comparator.naturalOrder());
    }

    public static <T> List<T> sort(List<T> input, Comparator<T> cmp) {
        MinHeap<T> heap = new MinHeap<>(cmp);
        for (T item : input) heap.insert(item);

        List<T> sorted = new ArrayList<>(input.size());
        while (!heap.isEmpty()) sorted.add(heap.extractMin());
        return sorted;
    }
}
