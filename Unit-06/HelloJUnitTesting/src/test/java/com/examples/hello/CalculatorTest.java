package com.examples.hello;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Calculator}.
 *
 * <p>Each test method:
 * <ul>
 *   <li>Is annotated with {@code @Test} so JUnit discovers it automatically.</li>
 *   <li>Has a human-readable name via {@code @DisplayName}.</li>
 *   <li>Calls one {@code assert…} method to verify the expected outcome.</li>
 * </ul>
 */
@DisplayName("Calculator")
class CalculatorTest {

    // A single Calculator instance shared by all tests.
    // Because Calculator has no state, there is no need to recreate it before each test.
    private final Calculator calc = new Calculator();

    @Test
    @DisplayName("add: 2 + 3 = 5")
    void addsTwoNumbers() {
        assertEquals(5, calc.add(2, 3));
    }

    @Test
    @DisplayName("add: negative numbers work correctly")
    void addsNegativeNumbers() {
        assertEquals(-1, calc.add(-4, 3));
    }

    @Test
    @DisplayName("subtract: 10 - 4 = 6")
    void subtractsTwoNumbers() {
        assertEquals(6, calc.subtract(10, 4));
    }

    @Test
    @DisplayName("divide: 10 / 2 = 5")
    void dividesTwoNumbers() {
        assertEquals(6, calc.divide(10, 2));
    }

    @Test
    @DisplayName("divide: throws ArithmeticException when divisor is zero")
    void divideByZeroThrows() {
        // assertThrows verifies that the lambda throws the specified exception type.
        ArithmeticException ex = assertThrows(
                ArithmeticException.class,
                () -> calc.divide(10, 0));

        assertEquals("Cannot divide by zero", ex.getMessage());
    }
}
