package com.lambda.patterns;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

/**
 * Functional programming patterns built from first-class functions.
 *
 * <p><strong>Partial application</strong> — fix some arguments of a function,
 * returning a new function with fewer parameters.  Useful when one argument
 * is fixed for a whole section of code.
 *
 * <p><strong>Currying</strong> — transform a two-argument function into a
 * chain of one-argument functions: {@code f(a, b) → g(a)(b)}.  Partial
 * application then becomes simple: call g(a) to get a specialised function.
 *
 * <p><strong>Memoization</strong> — cache the result of a pure function
 * keyed by its arguments.  Safe only for pure (referentially transparent)
 * functions; never memoize functions with side effects.
 *
 * <p><strong>Pipeline builder</strong> — assemble a sequence of processing
 * steps at runtime, then apply them to each item in a collection.
 */
public class FunctionPatterns {

    // -----------------------------------------------------------------------
    // Partial application
    // -----------------------------------------------------------------------

    public void showPartialApplication() {
        System.out.println("-- Partial application --");

        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);

        // Fix the first argument — produce a specialised single-arg function.
        Function<Integer, String> repeatHello = n -> repeat.apply("hello ", n);
        System.out.println("  repeatHello(3) = " + repeatHello.apply(3));

        // Helper that returns a partially-applied version of any BiFunction.
        Function<Integer, String> repeatStar = partialLeft(repeat, "* ");
        System.out.println("  repeatStar(5)  = " + repeatStar.apply(5));

        // Fix the SECOND argument instead.
        Function<String, String> repeat4Times = partialRight(repeat, 4);
        System.out.println("  repeat4Times(\"ab\") = " + repeat4Times.apply("ab"));
    }

    private static <A, B, R> Function<B, R> partialLeft(BiFunction<A, B, R> f, A a) {
        return b -> f.apply(a, b);
    }

    private static <A, B, R> Function<A, R> partialRight(BiFunction<A, B, R> f, B b) {
        return a -> f.apply(a, b);
    }

    // -----------------------------------------------------------------------
    // Currying
    // -----------------------------------------------------------------------

    public void showCurrying() {
        System.out.println("\n-- Currying --");

        // A curried add: instead of (a, b) → a + b,
        // it's a → (b → a + b).
        Function<Integer, Function<Integer, Integer>> curriedAdd = a -> b -> a + b;

        Function<Integer, Integer> add10 = curriedAdd.apply(10); // partial apply
        System.out.println("  add10(5)  = " + add10.apply(5));
        System.out.println("  add10(20) = " + add10.apply(20));

        // Curried string formatter.
        Function<String, Function<String, String>> format =
            prefix -> suffix -> prefix + " | " + suffix;

        Function<String, String> tagWith = format.apply("[INFO]");
        System.out.println("  tagWith(\"server started\") = " + tagWith.apply("server started"));
        System.out.println("  tagWith(\"shutdown complete\") = " + tagWith.apply("shutdown complete"));

        // Utility to curry any BiFunction.
        BiFunction<Double, Double, Double> pow = Math::pow;
        Function<Double, Function<Double, Double>> curriedPow = curry(pow);
        Function<Double, Double> square = curriedPow.apply(2.0);  // base=2
        System.out.println("  square(10) = " + square.apply(10.0));
    }

    private static <A, B, R> Function<A, Function<B, R>> curry(BiFunction<A, B, R> f) {
        return a -> b -> f.apply(a, b);
    }

    // -----------------------------------------------------------------------
    // Memoization
    // -----------------------------------------------------------------------

    public void showMemoization() {
        System.out.println("\n-- Memoization --");

        // Simulate an expensive computation.
        Function<Integer, Long> expensive = n -> {
            System.out.println("    computing fib(" + n + ")...");
            return slowFib(n);
        };

        Function<Integer, Long> memoFib = memoize(expensive);

        System.out.println("  fib(10) = " + memoFib.apply(10));
        System.out.println("  fib(10) = " + memoFib.apply(10));  // cached, no print
        System.out.println("  fib(15) = " + memoFib.apply(15));
        System.out.println("  fib(15) = " + memoFib.apply(15));  // cached

        // Thread-safe version using ConcurrentHashMap.computeIfAbsent.
        Function<Integer, Long> safeMemo = memoizeConcurrent(n -> {
            System.out.println("    concurrent: computing fib(" + n + ")...");
            return slowFib(n);
        });
        System.out.println("  concurrent fib(20) = " + safeMemo.apply(20));
        System.out.println("  concurrent fib(20) = " + safeMemo.apply(20));
    }

    private static <A, B> Function<A, B> memoize(Function<A, B> f) {
        Map<A, B> cache = new HashMap<>();
        return a -> cache.computeIfAbsent(a, f);
    }

    private static <A, B> Function<A, B> memoizeConcurrent(Function<A, B> f) {
        Map<A, B> cache = new ConcurrentHashMap<>();
        return a -> cache.computeIfAbsent(a, f);
    }

    private static long slowFib(int n) {
        if (n <= 1) return n;
        return slowFib(n - 1) + slowFib(n - 2);
    }

    // -----------------------------------------------------------------------
    // Pipeline builder
    // -----------------------------------------------------------------------

    public void showPipeline() {
        System.out.println("\n-- Pipeline builder --");

        // A Pipeline<T> accumulates transformation steps and applies them in order.
        Pipeline<String> cleanAndTag = Pipeline.<String>start()
            .addStep(String::trim)
            .addStep(String::toLowerCase)
            .addStep(s -> "[" + s + "]");

        List<String> inputs = List.of("  Hello  ", " WORLD ", "  Java 21 ");
        System.out.println("  raw input:  " + inputs);
        System.out.println("  processed:  " + inputs.stream().map(cleanAndTag::run).toList());

        // A numeric pipeline.
        Pipeline<Double> normalize = Pipeline.<Double>start()
            .addStep(d -> d - 32)       // Fahrenheit offset
            .addStep(d -> d * 5 / 9);   // to Celsius

        List<Double> temps = List.of(32.0, 212.0, 98.6, -40.0);
        System.out.println("  F temps: " + temps);
        System.out.println("  C temps: " + temps.stream().map(normalize::run)
            .map("%.1f"::formatted).toList());
    }

    static final class Pipeline<T> {
        private final Function<T, T> composed;

        private Pipeline(Function<T, T> f) { this.composed = f; }

        static <T> Pipeline<T> start() { return new Pipeline<>(Function.identity()); }

        Pipeline<T> addStep(Function<T, T> step) {
            return new Pipeline<>(composed.andThen(step));
        }

        T run(T input) { return composed.apply(input); }
    }
}
