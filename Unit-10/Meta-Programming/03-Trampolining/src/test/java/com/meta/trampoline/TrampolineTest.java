package com.meta.trampoline;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrampolineTest {

    @Test @DisplayName("Bounce.done() holds value, run() returns it immediately")
    void done_returnsValue() {
        assertEquals(42, Bounce.run(Bounce.done(42)));
    }

    @Test @DisplayName("Bounce.more() with single step resolves correctly")
    void more_singleStep() {
        Bounce<String> b = Bounce.more(() -> Bounce.done("hello"));
        assertEquals("hello", Bounce.run(b));
    }

    @Test @DisplayName("Bounce chain of 1000 steps resolves without stack overflow")
    void moreChain_deepWithoutOverflow() {
        Bounce<Integer> chain = countDown(1000);
        assertEquals(0, Bounce.run(chain));
    }

    private Bounce<Integer> countDown(int n) {
        return n == 0 ? Bounce.done(0) : Bounce.more(() -> countDown(n - 1));
    }

    @Test @DisplayName("factorial(0) = 1")
    void factorial_zero() {
        assertEquals(1L, TrampolinedFunctions.factorial(0));
    }

    @Test @DisplayName("factorial(1) = 1")
    void factorial_one() {
        assertEquals(1L, TrampolinedFunctions.factorial(1));
    }

    @Test @DisplayName("factorial(5) = 120")
    void factorial_five() {
        assertEquals(120L, TrampolinedFunctions.factorial(5));
    }

    @Test @DisplayName("factorial(10) = 3628800")
    void factorial_ten() {
        assertEquals(3628800L, TrampolinedFunctions.factorial(10));
    }

    @Test @DisplayName("factorial(50000) runs without StackOverflowError")
    void factorial_deep_noOverflow() {
        assertDoesNotThrow(() -> TrampolinedFunctions.factorial(50_000));
    }

    @Test @DisplayName("sumTo(n) = n*(n+1)/2")
    void sumTo_formula() {
        for (int n : new int[]{ 0, 1, 10, 100, 1000 }) {
            long expected = (long) n * (n + 1) / 2;
            assertEquals(expected, TrampolinedFunctions.sumTo(n), "sumTo(" + n + ")");
        }
    }

    @Test @DisplayName("sumTo(100000) runs without StackOverflowError")
    void sumTo_deep_noOverflow() {
        assertDoesNotThrow(() -> TrampolinedFunctions.sumTo(100_000));
    }

    @Test @DisplayName("isEven: even numbers return true")
    void isEven_even() {
        assertTrue(TrampolinedFunctions.isEven(0));
        assertTrue(TrampolinedFunctions.isEven(2));
        assertTrue(TrampolinedFunctions.isEven(100));
    }

    @Test @DisplayName("isEven: odd numbers return false")
    void isEven_odd() {
        assertFalse(TrampolinedFunctions.isEven(1));
        assertFalse(TrampolinedFunctions.isEven(3));
        assertFalse(TrampolinedFunctions.isEven(99));
    }

    @Test @DisplayName("isOdd: odd numbers return true")
    void isOdd_odd() {
        assertTrue(TrampolinedFunctions.isOdd(1));
        assertTrue(TrampolinedFunctions.isOdd(7));
    }

    @Test @DisplayName("isEven(50000) runs without StackOverflowError")
    void isEven_deep_noOverflow() {
        assertDoesNotThrow(() -> TrampolinedFunctions.isEven(50_000));
    }

    @Test @DisplayName("fibonacci(0) = 0, fibonacci(1) = 1")
    void fibonacci_base() {
        assertEquals(0L, TrampolinedFunctions.fibonacci(0));
        assertEquals(1L, TrampolinedFunctions.fibonacci(1));
    }

    @Test @DisplayName("fibonacci(10) = 55")
    void fibonacci_ten() {
        assertEquals(55L, TrampolinedFunctions.fibonacci(10));
    }

    @Test @DisplayName("fibonacci(20) = 6765")
    void fibonacci_twenty() {
        assertEquals(6765L, TrampolinedFunctions.fibonacci(20));
    }

    @Test @DisplayName("Naive factorial overflows at large n (baseline)")
    void factorialNaive_overflows() {
        assertThrows(StackOverflowError.class,
            () -> TrampolinedFunctions.factorialNaive(50_000));
    }
}
