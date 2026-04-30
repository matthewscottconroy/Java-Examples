package com.algorithms.search.binary;

import java.util.Comparator;
import java.util.List;

/**
 * Binary search variants on sorted random-access collections.
 *
 * <p>All methods assume the input is sorted according to the provided comparator.
 * If the input is unsorted the results are undefined.
 */
public final class BinarySearch {

    private BinarySearch() {}

    /**
     * Returns the index of {@code target} in {@code list}, or -1 if absent.
     * When duplicates are present, returns an arbitrary matching index.
     */
    public static <T> int search(List<T> list, T target, Comparator<T> cmp) {
        int lo = 0, hi = list.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int c = cmp.compare(list.get(mid), target);
            if      (c < 0) lo = mid + 1;
            else if (c > 0) hi = mid - 1;
            else            return mid;
        }
        return -1;
    }

    /**
     * Returns the index of the first element equal to {@code target}, or -1.
     */
    public static <T> int searchFirst(List<T> list, T target, Comparator<T> cmp) {
        int lo = 0, hi = list.size() - 1, result = -1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int c = cmp.compare(list.get(mid), target);
            if      (c < 0) lo = mid + 1;
            else if (c > 0) hi = mid - 1;
            else  { result = mid; hi = mid - 1; }  // keep searching left
        }
        return result;
    }

    /**
     * Returns the index of the last element equal to {@code target}, or -1.
     */
    public static <T> int searchLast(List<T> list, T target, Comparator<T> cmp) {
        int lo = 0, hi = list.size() - 1, result = -1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int c = cmp.compare(list.get(mid), target);
            if      (c < 0) lo = mid + 1;
            else if (c > 0) hi = mid - 1;
            else  { result = mid; lo = mid + 1; }  // keep searching right
        }
        return result;
    }

    /**
     * Returns the index of the first element greater than {@code target} (upper bound),
     * or {@code list.size()} if all elements are ≤ target.
     */
    public static <T> int upperBound(List<T> list, T target, Comparator<T> cmp) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (cmp.compare(list.get(mid), target) <= 0) lo = mid + 1;
            else                                          hi = mid;
        }
        return lo;
    }

    /**
     * Returns the index of the first element ≥ {@code target} (lower bound),
     * or {@code list.size()} if all elements are < target.
     */
    public static <T> int lowerBound(List<T> list, T target, Comparator<T> cmp) {
        int lo = 0, hi = list.size();
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (cmp.compare(list.get(mid), target) < 0) lo = mid + 1;
            else                                         hi = mid;
        }
        return lo;
    }
}
