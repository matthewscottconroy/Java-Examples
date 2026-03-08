package com.examples.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A typed, immutable list of elements in a specific order, together with
 * permutation operations.
 *
 * <p>An {@code Arrangement<T>} is the concrete thing a permutation acts on.
 * If a {@link Permutation} is the abstract "shuffle", an Arrangement is
 * the actual deck of cards being shuffled.
 *
 * <pre>
 *   Arrangement&lt;String&gt; lineup = Arrangement.of("Alice", "Bob", "Carol", "Dave");
 *   lineup.countOrderings();   // 24  (= 4!)
 *   lineup.permute(p);         // a new arrangement after applying permutation p
 *   lineup.allOrderings();     // all 24 possible lineups
 * </pre>
 *
 * <p>Use {@link #map(Function)} to relabel the elements and pass the
 * arrangement to a {@link StructureMap}.
 */
public final class Arrangement<T> {

    private final List<T> elements;

    public Arrangement(List<T> elements) {
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    /** Convenience factory method for creating an arrangement from varargs. */
    @SafeVarargs
    public static <T> Arrangement<T> of(T... items) {
        return new Arrangement<>(Arrays.asList(items));
    }

    /** Returns the number of elements in this arrangement. */
    public int size() {
        return elements.size();
    }

    /** Returns the element at position {@code i}. */
    public T get(int i) {
        return elements.get(i);
    }

    /** Returns an unmodifiable view of the underlying list. */
    public List<T> toList() {
        return elements;
    }

    /**
     * Returns a new arrangement produced by applying permutation {@code p}.
     * Position {@code i} of the result holds whatever was at index {@code p.map(i)}.
     */
    public Arrangement<T> permute(Permutation p) {
        if (p.size() != elements.size()) {
            throw new IllegalArgumentException(
                    "Permutation size " + p.size() + " doesn't match arrangement size " + elements.size());
        }
        return new Arrangement<>(p.apply(elements));
    }

    /**
     * Returns the number of distinct orderings: n! where n = {@link #size()}.
     * This is how many different arrangements you can make from the same elements.
     */
    public long countOrderings() {
        return Permutation.factorial(elements.size());
    }

    /**
     * Returns all possible orderings of this arrangement's elements.
     * <strong>Warning:</strong> the list has n! entries — use only for small n.
     */
    public List<Arrangement<T>> allOrderings() {
        List<List<T>> perms = new ArrayList<>();
        heapPermute(new ArrayList<>(elements), elements.size(), perms);
        List<Arrangement<T>> result = new ArrayList<>(perms.size());
        for (List<T> p : perms) {
            result.add(new Arrangement<>(p));
        }
        return result;
    }

    /**
     * Returns a new arrangement with every element transformed by {@code f}.
     * The order is preserved; only the labels change.
     */
    public <U> Arrangement<U> map(Function<T, U> f) {
        List<U> mapped = new ArrayList<>(elements.size());
        for (T item : elements) {
            mapped.add(f.apply(item));
        }
        return new Arrangement<>(mapped);
    }

    /**
     * Finds the permutation that transforms this arrangement into {@code target}.
     * Returns the permutation σ such that {@code this.permute(σ).equals(target)}.
     *
     * @throws IllegalArgumentException if target contains different elements
     */
    public Permutation permutationTo(Arrangement<T> target) {
        if (target.size() != size()) {
            throw new IllegalArgumentException("Arrangements have different sizes.");
        }
        int n = size();
        int[] mapping = new int[n];
        for (int i = 0; i < n; i++) {
            T want = target.get(i);
            boolean found = false;
            for (int j = 0; j < n; j++) {
                if (elements.get(j).equals(want)) {
                    mapping[i] = j;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException(
                        "Element " + want + " from target not found in this arrangement.");
            }
        }
        return new Permutation(mapping);
    }

    // Heap's algorithm for generating all permutations in-place
    private void heapPermute(List<T> arr, int k, List<List<T>> result) {
        if (k == 1) {
            result.add(new ArrayList<>(arr));
            return;
        }
        for (int i = 0; i < k; i++) {
            heapPermute(arr, k - 1, result);
            if (k % 2 == 0) {
                Collections.swap(arr, i, k - 1);
            } else {
                Collections.swap(arr, 0, k - 1);
            }
        }
    }

    @Override
    public String toString() {
        return elements.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Arrangement<?> a)) return false;
        return elements.equals(a.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}
