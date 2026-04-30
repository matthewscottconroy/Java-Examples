package com.functional.recursion;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility for wrapping a recursive function with memoization.
 *
 * <p>Memoization caches the result of each unique input so that repeated calls
 * with the same argument return the cached value in O(1) instead of
 * re-running the computation.
 *
 * <p>The Y-combinator trick used here lets a lambda refer to itself — normally
 * impossible in Java because the variable is not yet assigned when the lambda
 * captures it.
 */
public final class Memo {

    private Memo() {}

    /**
     * Memoize a function that calls itself through a provided self-reference.
     *
     * <p>Usage:
     * <pre>{@code
     * Function<Integer, Long> fib = Memo.memoize(self -> n ->
     *     n <= 1 ? n : self.apply(n - 1) + self.apply(n - 2));
     * }</pre>
     *
     * @param fn a function that accepts a self-reference and returns the actual function
     */
    public static <I, O> Function<I, O> memoize(Function<Function<I, O>, Function<I, O>> fn) {
        Map<I, O> cache = new HashMap<>();
        Function<I, O>[] self = new Function[1];
        // computeIfAbsent throws ConcurrentModificationException when the mapping function
        // recursively re-enters the same map (JDK 9+). Explicit check-then-put avoids this.
        self[0] = input -> {
            if (cache.containsKey(input)) return cache.get(input);
            O result = fn.apply(self[0]).apply(input);
            cache.put(input, result);
            return result;
        };
        return self[0];
    }

    /**
     * Simple memoization for a plain function (no self-reference needed).
     */
    public static <I, O> Function<I, O> of(Function<I, O> fn) {
        Map<I, O> cache = new HashMap<>();
        return input -> cache.computeIfAbsent(input, fn);
    }
}
