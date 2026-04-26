package com.generics.bounded;

/**
 * Multiple bounds: {@code <T extends A & B & C>}.
 *
 * <p>Rules:
 * <ul>
 *   <li>T must satisfy <em>all</em> listed bounds simultaneously (intersection).</li>
 *   <li>At most one bound may be a class; if present it must come <em>first</em>.</li>
 *   <li>All remaining bounds must be interfaces.</li>
 * </ul>
 *
 * <p>Inside the method body, the full API of every bound is available.
 */
public class MultiBounded {

    /**
     * Clamps {@code value} to the range {@code [min, max]}.
     *
     * <p>T must be a Number (so we could do arithmetic) AND Comparable to itself
     * (so we can compare with min/max).  Both constraints are required — neither
     * alone is enough.
     *
     * <p>Note the bound order: {@code Number} (class) comes before
     * {@code Comparable<T>} (interface).
     */
    public static <T extends Number & Comparable<T>> T clamp(T value, T min, T max) {
        if (value.compareTo(min) < 0) return min;
        if (value.compareTo(max) > 0) return max;
        return value;
    }

    /**
     * Returns the sum of two numbers as a {@code double}.
     *
     * <p>Uses only the Number bound here (Comparable not needed), but the
     * method still accepts any type that satisfies {@code Number & Comparable<T>}.
     */
    public static <T extends Number & Comparable<T>> double sum(T a, T b) {
        return a.doubleValue() + b.doubleValue();
    }

    /**
     * Returns the smaller of two values that are both Comparable and Cloneable.
     *
     * <p>Both bounds are interfaces — either order is valid, but alphabetical is common.
     * Cloneable is included purely to show a second interface bound; in practice
     * you would omit it if it's not needed.
     */
    public static <T extends Comparable<T> & Cloneable> T minOf(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
}
