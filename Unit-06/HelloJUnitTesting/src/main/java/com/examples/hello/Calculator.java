package com.examples.hello;

/**
 * Simple integer arithmetic — the subject under test.
 *
 * <p>Having a dedicated class with clearly defined behaviour makes it
 * straightforward to write tests: given an input, assert an output.
 */
public class Calculator {

    /** Returns the sum of {@code a} and {@code b}. */
    public int add(int a, int b) {
        return a + b;
    }

    /** Returns the difference {@code a - b}. */
    public int subtract(int a, int b) {
        return a - b;
    }

    /**
     * Returns the integer quotient {@code a / b}.
     *
     * @throws ArithmeticException if {@code b} is zero
     */
    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return a / b;
    }
}
