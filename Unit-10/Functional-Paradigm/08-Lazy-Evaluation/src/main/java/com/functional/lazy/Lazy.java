package com.functional.lazy;

import java.util.function.Supplier;

/**
 * A lazily-evaluated value: the supplier is called at most once, on first access.
 *
 * <p>This is the canonical Java implementation of a lazy thunk: wrap an
 * expensive computation in a {@link Supplier}, then memoize the result so
 * subsequent calls return the cached value without re-running the computation.
 *
 * @param <T> type of the wrapped value
 */
public final class Lazy<T> {

    private final Supplier<T> supplier;
    private T   value;
    private boolean computed = false;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Create a lazy value from a supplier.
     *
     * @param supplier the computation to defer
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Evaluate and return the value, computing it on the first call only.
     */
    public T get() {
        if (!computed) {
            value    = supplier.get();
            computed = true;
        }
        return value;
    }

    /** True if the value has already been computed. */
    public boolean isComputed() { return computed; }
}
