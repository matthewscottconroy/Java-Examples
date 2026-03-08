package com.examples.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An immutable permutation of n positions.
 *
 * <p>A permutation is a bijection σ: {0,…,n−1} → {0,…,n−1}. Think of it as
 * a lookup table for a "shuffle": when you apply σ to a list, position {@code i}
 * of the result holds whatever was at index {@code σ(i)} in the original.
 *
 * <pre>
 *   original:  [Alice, Bob, Carol, Dave]
 *   σ = [2,0,3,1]
 *   result:    [Carol, Alice, Dave, Bob]
 *                ↑       ↑     ↑    ↑
 *          σ(0)=2   σ(1)=0  σ(2)=3  σ(3)=1
 * </pre>
 *
 * <p>Permutations can be composed, inverted, and expressed in cycle notation.
 * All permutations of n items form the <em>symmetric group</em> S_n, which has
 * exactly n! elements.
 */
public final class Permutation {

    private final int[] mapping;

    /**
     * Constructs a permutation from a mapping array.
     *
     * @param mapping {@code mapping[i] = j} means "take the element at index j
     *                and place it at position i"
     * @throws IllegalArgumentException if the array is not a valid bijection
     */
    public Permutation(int[] mapping) {
        validate(mapping);
        this.mapping = Arrays.copyOf(mapping, mapping.length);
    }

    /** Returns the number of positions this permutation acts on. */
    public int size() {
        return mapping.length;
    }

    /** Returns where position {@code i} draws its element from: σ(i). */
    public int map(int i) {
        return mapping[i];
    }

    /**
     * Applies this permutation to a list. Returns a new list where
     * {@code result.get(i) == items.get(mapping[i])}.
     */
    public <T> List<T> apply(List<T> items) {
        if (items.size() != mapping.length) {
            throw new IllegalArgumentException(
                    "List size " + items.size() + " does not match permutation size " + mapping.length);
        }
        List<T> result = new ArrayList<>(mapping.length);
        for (int i : mapping) {
            result.add(items.get(i));
        }
        return result;
    }

    /**
     * Composes this permutation with {@code other}: first apply {@code this},
     * then {@code other}. The result σ satisfies {@code σ(i) = other(this(i))}.
     */
    public Permutation thenApply(Permutation other) {
        if (this.mapping.length != other.mapping.length) {
            throw new IllegalArgumentException("Cannot compose permutations of different sizes.");
        }
        int[] composed = new int[mapping.length];
        for (int i = 0; i < mapping.length; i++) {
            composed[i] = other.mapping[this.mapping[i]];
        }
        return new Permutation(composed);
    }

    /**
     * Returns the inverse permutation σ⁻¹: if σ maps i → j, then σ⁻¹ maps j → i.
     * Applying σ then σ⁻¹ (or vice versa) gives the identity.
     */
    public Permutation inverse() {
        int[] inv = new int[mapping.length];
        for (int i = 0; i < mapping.length; i++) {
            inv[mapping[i]] = i;
        }
        return new Permutation(inv);
    }

    /** Returns {@code true} if this permutation leaves every position unchanged. */
    public boolean isIdentity() {
        for (int i = 0; i < mapping.length; i++) {
            if (mapping[i] != i) return false;
        }
        return true;
    }

    /**
     * Returns the order of this permutation: the smallest positive k such that
     * applying it k times returns to the identity. For example, a swap has order 2
     * (swap twice = back to start), and a 3-cycle has order 3.
     */
    public int order() {
        Permutation power = this;
        for (int k = 1; k <= factorial(mapping.length); k++) {
            if (power.isIdentity()) return k;
            power = power.thenApply(this);
        }
        return mapping.length; // unreachable for valid permutations
    }

    /**
     * Returns cycle notation, e.g., {@code (0 2 3)(1)}.
     *
     * <p>A cycle {@code (a b c)} means: the element at a goes to b, b goes to c,
     * and c wraps back to a. Fixed points (cycles of length 1) are shown explicitly.
     */
    public String toCycleNotation() {
        boolean[] visited = new boolean[mapping.length];
        StringBuilder sb = new StringBuilder();
        for (int start = 0; start < mapping.length; start++) {
            if (!visited[start]) {
                List<Integer> cycle = new ArrayList<>();
                int cur = start;
                while (!visited[cur]) {
                    visited[cur] = true;
                    cycle.add(cur);
                    cur = mapping[cur];
                }
                sb.append("(");
                for (int j = 0; j < cycle.size(); j++) {
                    if (j > 0) sb.append(" ");
                    sb.append(cycle.get(j));
                }
                sb.append(")");
            }
        }
        return sb.toString();
    }

    /** Returns the identity permutation on n positions (does nothing). */
    public static Permutation identity(int n) {
        int[] id = new int[n];
        for (int i = 0; i < n; i++) id[i] = i;
        return new Permutation(id);
    }

    /**
     * Returns a left-rotation permutation: each element shifts left by {@code k}
     * positions, and the first k elements wrap around to the end.
     *
     * <pre>
     *   rotation(4, 1):  [A, B, C, D] → [B, C, D, A]
     * </pre>
     */
    public static Permutation rotation(int n, int k) {
        k = ((k % n) + n) % n; // normalize to [0, n)
        int[] rot = new int[n];
        for (int i = 0; i < n; i++) {
            rot[i] = (i + k) % n;
        }
        return new Permutation(rot);
    }

    /**
     * Returns a swap (transposition) of positions {@code i} and {@code j}.
     * Everything else stays put.
     */
    public static Permutation swap(int n, int i, int j) {
        int[] s = new int[n];
        for (int k = 0; k < n; k++) s[k] = k;
        s[i] = j;
        s[j] = i;
        return new Permutation(s);
    }

    /** Returns a raw copy of the internal mapping array. */
    public int[] toArray() {
        return Arrays.copyOf(mapping, mapping.length);
    }

    private static void validate(int[] mapping) {
        boolean[] seen = new boolean[mapping.length];
        for (int v : mapping) {
            if (v < 0 || v >= mapping.length) {
                throw new IllegalArgumentException(
                        "Value " + v + " out of range for permutation of size " + mapping.length);
            }
            if (seen[v]) {
                throw new IllegalArgumentException(
                        "Duplicate value " + v + " — not a valid permutation: " + Arrays.toString(mapping));
            }
            seen[v] = true;
        }
    }

    static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permutation p)) return false;
        return Arrays.equals(mapping, p.mapping);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mapping);
    }

    @Override
    public String toString() {
        return Arrays.toString(mapping);
    }
}
