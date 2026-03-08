package com.examples.math;

/**
 * A unit of measurement.
 *
 * <p>Every concrete unit knows how to convert a value to and from a shared
 * <em>base unit</em> for its category (e.g., grams for weight, meters for
 * distance, seconds for time). Converting between any two units A and B
 * always goes through the base:
 *
 * <pre>
 *   A  →  base  →  B
 * </pre>
 *
 * <p>This means adding a new unit only requires one relationship (to the base),
 * rather than a conversion entry for every possible pair.
 *
 * <p>This interface is implemented by the {@link WeightUnit}, {@link DistanceUnit},
 * and {@link TimeUnit} enums.
 */
public interface Unit {

    /**
     * Returns the short symbol for this unit, e.g., {@code "kg"}, {@code "mi"},
     * {@code "hr"}.
     */
    String symbol();

    /**
     * Returns the full human-readable name, e.g., {@code "kilograms"},
     * {@code "miles"}, {@code "hours"}.
     */
    String fullName();

    /**
     * Converts {@code value} expressed in this unit into the base unit.
     *
     * @param value a measurement in this unit
     * @return the equivalent value in the base unit
     */
    double toBase(double value);

    /**
     * Converts {@code value} expressed in the base unit into this unit.
     *
     * @param value a measurement in the base unit
     * @return the equivalent value in this unit
     */
    double fromBase(double value);
}
