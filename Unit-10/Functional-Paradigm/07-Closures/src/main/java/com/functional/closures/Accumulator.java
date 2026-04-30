package com.functional.closures;

/**
 * A functional interface for an accumulator: takes a value, adds it to an
 * internal running total, and returns the new total.
 */
@FunctionalInterface
public interface Accumulator {
    double add(double value);
}
