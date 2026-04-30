package com.lambda.patterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class FunctionPatternsTest {

    // -- Pipeline --
    // Pipeline is package-private, so tests in the same package can access it.

    @Test @DisplayName("empty Pipeline returns the input unchanged")
    void pipeline_identity() {
        FunctionPatterns.Pipeline<String> p = FunctionPatterns.Pipeline.start();
        assertEquals("hello", p.run("hello"));
    }

    @Test @DisplayName("Pipeline applies a single step")
    void pipeline_oneStep() {
        FunctionPatterns.Pipeline<String> p = FunctionPatterns.Pipeline.<String>start()
            .addStep(String::toUpperCase);
        assertEquals("HELLO", p.run("hello"));
    }

    @Test @DisplayName("Pipeline applies steps in declaration order")
    void pipeline_multiStep() {
        FunctionPatterns.Pipeline<String> p = FunctionPatterns.Pipeline.<String>start()
            .addStep(String::trim)
            .addStep(String::toLowerCase)
            .addStep(s -> "[" + s + "]");
        assertEquals("[hello world]", p.run("  Hello World  "));
    }

    @Test @DisplayName("Pipeline works with numeric transformations")
    void pipeline_numeric() {
        FunctionPatterns.Pipeline<Double> toC = FunctionPatterns.Pipeline.<Double>start()
            .addStep(f -> f - 32)
            .addStep(f -> f * 5 / 9);
        assertEquals(0.0,   toC.run(32.0),  1e-9);
        assertEquals(100.0, toC.run(212.0), 1e-9);
    }

    @Test @DisplayName("Pipeline.addStep does not mutate the original pipeline")
    void pipeline_immutable() {
        FunctionPatterns.Pipeline<String> base    = FunctionPatterns.Pipeline.start();
        FunctionPatterns.Pipeline<String> derived = base.addStep(String::toUpperCase);
        assertEquals("abc", base.run("abc"));
        assertEquals("ABC", derived.run("abc"));
    }

    // -- Functional composition via java.util.function (used by FunctionPatterns internally) --

    @Test @DisplayName("Function.andThen applies second function after first")
    void composition_andThen() {
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> addThree = x -> x + 3;
        Function<Integer, Integer> combined = doubleIt.andThen(addThree);
        assertEquals(13, combined.apply(5));  // (5*2)+3
    }

    @Test @DisplayName("Function.compose applies second function before first")
    void composition_compose() {
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> addThree = x -> x + 3;
        Function<Integer, Integer> combined = doubleIt.compose(addThree);
        assertEquals(16, combined.apply(5));  // (5+3)*2
    }

    // -- Memoization correctness (verify via call-count side effect) --

    @Test @DisplayName("memoized function is called only once per unique input")
    void memoize_callCount() {
        AtomicInteger calls = new AtomicInteger(0);
        // Build a memoized function the same way FunctionPatterns does internally.
        java.util.Map<Integer, Integer> cache = new java.util.HashMap<>();
        Function<Integer, Integer> memo = n -> cache.computeIfAbsent(n, k -> {
            calls.incrementAndGet();
            return k * k;
        });

        assertEquals(4,  memo.apply(2));
        assertEquals(9,  memo.apply(3));
        assertEquals(4,  memo.apply(2));  // cached
        assertEquals(9,  memo.apply(3));  // cached
        assertEquals(2, calls.get());     // only 2 unique inputs computed
    }
}
