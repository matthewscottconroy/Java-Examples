package com.meta.trampoline;

/**
 * Classic recursive functions rewritten to use the trampoline pattern,
 * making them stack-safe for arbitrarily deep inputs.
 */
public class TrampolinedFunctions {

    // -----------------------------------------------------------------
    // Factorial (tail-recursive with accumulator)
    // -----------------------------------------------------------------

    /**
     * Naive recursive factorial — overflows the stack around n ≈ 8,000.
     * Included only to demonstrate the problem; do not call with large n.
     */
    public static long factorialNaive(int n) {
        return n == 0 ? 1L : n * factorialNaive(n - 1);
    }

    /** Trampolined factorial — stack-safe for any n that fits in a long. */
    public static long factorial(int n) {
        return Bounce.run(factStep(n, 1L));
    }

    private static Bounce<Long> factStep(long n, long acc) {
        return n == 0
            ? Bounce.done(acc)
            : Bounce.more(() -> factStep(n - 1, n * acc));
    }

    // -----------------------------------------------------------------
    // Even / Odd (mutually recursive — each calls the other)
    // -----------------------------------------------------------------

    /**
     * Naive mutual recursion: isEven calls isOdd, isOdd calls isEven.
     * Overflows around n ≈ 4,000 on a typical JVM.
     */
    public static boolean isEvenNaive(int n) {
        return n == 0 || isOddNaive(n - 1);
    }

    public static boolean isOddNaive(int n) {
        return n != 0 && isEvenNaive(n - 1);
    }

    /**
     * Trampolined mutual recursion — isEven and isOdd return thunks
     * instead of calling each other directly. The trampoline drives both.
     */
    public static boolean isEven(int n) {
        return Bounce.run(evenStep(n));
    }

    public static boolean isOdd(int n) {
        return Bounce.run(oddStep(n));
    }

    private static Bounce<Boolean> evenStep(int n) {
        return n == 0
            ? Bounce.done(true)
            : Bounce.more(() -> oddStep(n - 1));
    }

    private static Bounce<Boolean> oddStep(int n) {
        return n == 0
            ? Bounce.done(false)
            : Bounce.more(() -> evenStep(n - 1));
    }

    // -----------------------------------------------------------------
    // Fibonacci (not naturally tail-recursive, but trampolinable with CPS)
    // -----------------------------------------------------------------

    /**
     * Continuation-passing style Fibonacci via trampoline.
     * The continuation {@code k} accumulates the result without growing the stack.
     */
    public static long fibonacci(int n) {
        return Bounce.run(fibCps(n, Bounce::done));
    }

    @FunctionalInterface
    interface Cont<A> {
        Bounce<A> apply(A value);
    }

    private static Bounce<Long> fibCps(int n, Cont<Long> k) {
        if (n <= 1) return k.apply((long) n);
        return Bounce.more(() -> fibCps(n - 1, v1 ->
            Bounce.more(() -> fibCps(n - 2, v2 ->
                k.apply(v1 + v2)))));
    }

    // -----------------------------------------------------------------
    // Sum of a range (simple tail-recursive example for benchmarking)
    // -----------------------------------------------------------------

    public static long sumTo(int n) {
        return Bounce.run(sumStep(n, 0L));
    }

    private static Bounce<Long> sumStep(long n, long acc) {
        return n == 0
            ? Bounce.done(acc)
            : Bounce.more(() -> sumStep(n - 1, acc + n));
    }
}
