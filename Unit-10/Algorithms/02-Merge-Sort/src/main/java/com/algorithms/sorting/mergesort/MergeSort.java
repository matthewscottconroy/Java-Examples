package com.algorithms.sorting.mergesort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Top-down merge sort — O(n log n) guaranteed.
 *
 * <p>The algorithm:
 * <ol>
 *   <li>Divide the array in half (base case: length ≤ 1 is already sorted).</li>
 *   <li>Recursively sort each half.</li>
 *   <li>Merge the two sorted halves into one sorted result.</li>
 * </ol>
 *
 * <p>The merge step is the key: two sorted lists can be merged in O(n) by
 * maintaining two pointers and always picking the smaller front element.
 */
public final class MergeSort {

    private MergeSort() {}

    /** Sort a list using the element's natural order. */
    public static <T extends Comparable<T>> List<T> sort(List<T> list) {
        return sort(list, Comparator.naturalOrder());
    }

    /** Sort a list using the given comparator. */
    public static <T> List<T> sort(List<T> list, Comparator<T> cmp) {
        if (list.size() <= 1) return new ArrayList<>(list);

        int mid = list.size() / 2;
        List<T> left  = sort(list.subList(0, mid), cmp);
        List<T> right = sort(list.subList(mid, list.size()), cmp);
        return merge(left, right, cmp);
    }

    /**
     * Merge two sorted lists into one sorted list in O(n) time.
     *
     * <p>This is also the operation used when two hospital systems each provide
     * a sorted list of patients and you need one combined sorted list without
     * re-sorting from scratch.
     */
    public static <T> List<T> merge(List<T> left, List<T> right, Comparator<T> cmp) {
        List<T> result = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        while (i < left.size())  result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }
}
