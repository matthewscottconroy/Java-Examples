package com.functional.closures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ClosuresTest {

    @Test
    @DisplayName("Counter starts at the given value")
    void counterStartValue() {
        IntSupplier counter = CounterFactory.counter(42, 1);
        assertEquals(42, counter.getAsInt());
    }

    @Test
    @DisplayName("Counter increments by the given step")
    void counterStep() {
        IntSupplier counter = CounterFactory.counter(0, 5);
        counter.getAsInt(); // 0
        assertEquals(5, counter.getAsInt());
        assertEquals(10, counter.getAsInt());
    }

    @Test
    @DisplayName("Two counters have independent state")
    void countersAreIndependent() {
        IntSupplier a = CounterFactory.counter(0, 1);
        IntSupplier b = CounterFactory.counter(100, 10);
        a.getAsInt(); a.getAsInt(); // advance a twice
        assertEquals(2,   a.getAsInt());
        assertEquals(100, b.getAsInt()); // b unaffected
    }

    @Test
    @DisplayName("Accumulator sums added values")
    void accumulatorSums() {
        Accumulator acc = CounterFactory.accumulator();
        assertEquals(10.0, acc.add(10.0), 0.001);
        assertEquals(25.0, acc.add(15.0), 0.001);
        assertEquals(30.0, acc.add(5.0),  0.001);
    }

    @Test
    @DisplayName("Two accumulators are independent")
    void accumulatorsAreIndependent() {
        Accumulator a = CounterFactory.accumulator();
        Accumulator b = CounterFactory.accumulator();
        a.add(100.0);
        assertEquals(50.0, b.add(50.0), 0.001);
    }

    @Test
    @DisplayName("Rate limiter allows calls up to the maximum")
    void rateLimiterAllowsUpToMax() {
        Supplier<Boolean> limiter = CounterFactory.rateLimiter(3, 10_000);
        assertTrue(limiter.get());
        assertTrue(limiter.get());
        assertTrue(limiter.get());
        assertFalse(limiter.get()); // 4th call in window is blocked
    }

    @Test
    @DisplayName("Two rate limiters are independent")
    void rateLimitersAreIndependent() {
        Supplier<Boolean> a = CounterFactory.rateLimiter(2, 10_000);
        Supplier<Boolean> b = CounterFactory.rateLimiter(2, 10_000);
        a.get(); a.get(); // exhaust a
        assertFalse(a.get());
        assertTrue(b.get()); // b unaffected
    }
}
