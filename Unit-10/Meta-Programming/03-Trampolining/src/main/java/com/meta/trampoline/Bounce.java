package com.meta.trampoline;

import java.util.function.Supplier;

/**
 * Trampoline for stack-safe recursion.
 *
 * <p>Deep recursion causes {@link StackOverflowError} because each call frame
 * occupies stack space. The trampoline pattern converts recursion into a loop
 * by representing each recursive step as a data value instead of a call:
 *
 * <ul>
 *   <li>{@link Done} — the computation is complete; hold the result.</li>
 *   <li>{@link More} — one more step is needed; hold a thunk (zero-arg lambda)
 *       that produces the next {@code Bounce}.</li>
 * </ul>
 *
 * <p>The {@link #run()} method drives the loop: it keeps calling the thunk
 * until it reaches a {@code Done}, never growing the Java call stack beyond
 * a constant depth.
 *
 * <pre>
 *   // Naive — overflows at ~8000 on typical JVM:
 *   int factorial(int n) { return n == 0 ? 1 : n * factorial(n - 1); }
 *
 *   // Trampolined — runs for n = 100,000 or more:
 *   Bounce&lt;Long&gt; factTramp(long n, long acc) {
 *       return n == 0 ? Bounce.done(acc)
 *                     : Bounce.more(() -> factTramp(n - 1, n * acc));
 *   }
 * </pre>
 */
public sealed interface Bounce<A> permits Bounce.Done, Bounce.More {

    record Done<A>(A value) implements Bounce<A> {}
    record More<A>(Supplier<Bounce<A>> next) implements Bounce<A> {}

    static <A> Bounce<A> done(A value)           { return new Done<>(value); }
    static <A> Bounce<A> more(Supplier<Bounce<A>> next) { return new More<>(next); }

    /**
     * Runs the trampoline to completion.
     *
     * <p>This is the only loop in the entire pattern — the recursive functions
     * themselves never call each other; they return thunks that this method
     * invokes one at a time.
     */
    static <A> A run(Bounce<A> bounce) {
        Bounce<A> current = bounce;
        while (current instanceof More<A> m) {
            current = m.next().get();
        }
        return ((Done<A>) current).value();
    }
}
