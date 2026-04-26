package com.generics.bounded;

import java.util.List;

/**
 * Generic algorithms that require ordering — demonstrating recursive bounds.
 *
 * <p><strong>Recursive bound:</strong> {@code <T extends Comparable<T>>}
 * means "T must be comparable to itself."  This is the standard Java pattern
 * for natural-ordering algorithms: it lets us call {@code a.compareTo(b)}
 * without knowing the concrete type at compile time.
 *
 * <p>Why "recursive"?  The bound mentions T inside its own definition:
 * <pre>
 *   T extends Comparable&lt;T&gt;
 *                         ^
 *                         T refers to the same T being defined
 * </pre>
 * Every class in Java that has a natural ordering (Integer, String, Double, …)
 * implements {@code Comparable<itself>}, so they all satisfy this bound.
 */
public class Algorithms {

    // Minimum of a non-empty list.
    public static <T extends Comparable<T>> T min(List<T> list) {
        if (list.isEmpty()) throw new IllegalArgumentException("List must not be empty");
        T result = list.get(0);
        for (T item : list) {
            if (item.compareTo(result) < 0) result = item;
        }
        return result;
    }

    // Maximum of a non-empty list.
    public static <T extends Comparable<T>> T max(List<T> list) {
        if (list.isEmpty()) throw new IllegalArgumentException("List must not be empty");
        T result = list.get(0);
        for (T item : list) {
            if (item.compareTo(result) > 0) result = item;
        }
        return result;
    }

    // Binary search on a sorted list — requires compareTo() at every step.
    public static <T extends Comparable<T>> int binarySearch(List<T> sorted, T target) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int cmp = sorted.get(mid).compareTo(target);
            if      (cmp < 0) lo = mid + 1;
            else if (cmp > 0) hi = mid - 1;
            else              return mid;
        }
        return -(lo + 1);   // not found: encodes insertion point as negative
    }
}
