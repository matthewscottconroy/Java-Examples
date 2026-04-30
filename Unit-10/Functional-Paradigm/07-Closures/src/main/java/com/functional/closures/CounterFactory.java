package com.functional.closures;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Factory methods that return lambdas closed over mutable state.
 *
 * <p>A <strong>closure</strong> is a function that captures variables from its
 * enclosing scope. Each call to a factory method here creates a <em>new</em>
 * closure with its own private copy of the captured state. The closures are
 * independent — incrementing one counter does not affect any other.
 */
public final class CounterFactory {

    private CounterFactory() {}

    /**
     * Return a counter that starts at {@code start} and increments by {@code step}.
     *
     * <p>The returned {@link IntSupplier} closes over a single-element array —
     * a common Java idiom since local variables captured by lambdas must be
     * effectively final, but array *contents* can be mutated.
     *
     * @param start initial value
     * @param step  amount added on each call
     */
    public static IntSupplier counter(int start, int step) {
        int[] state = { start };
        return () -> {
            int current = state[0];
            state[0] += step;
            return current;
        };
    }

    /**
     * Return an accumulator that sums all values passed to it.
     *
     * <p>The returned function closes over a {@code double[]} running total.
     *
     * @return a {@code java.util.function.Consumer<Double>}-like supplier;
     *         here modelled as a custom functional interface for clarity
     */
    public static Accumulator accumulator() {
        double[] total = { 0.0 };
        return value -> {
            total[0] += value;
            return total[0];
        };
    }

    /**
     * Return a rate-limiter that allows at most {@code maxCalls} invocations
     * per time window of {@code windowMs} milliseconds.
     *
     * <p>Each limiter is an independent closure over its own call log.
     */
    public static Supplier<Boolean> rateLimiter(int maxCalls, long windowMs) {
        long[] callTimes = new long[maxCalls];
        int[]  index     = { 0 };
        int[]  count     = { 0 };

        return () -> {
            long now = System.currentTimeMillis();
            int  idx = index[0] % maxCalls;

            if (count[0] < maxCalls) {
                callTimes[idx] = now;
                index[0]++;
                count[0]++;
                return true;
            }

            long oldest = callTimes[idx];
            if (now - oldest >= windowMs) {
                callTimes[idx] = now;
                index[0]++;
                return true;
            }
            return false;
        };
    }
}
