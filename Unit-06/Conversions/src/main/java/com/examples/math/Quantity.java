package com.examples.math;

/**
 * A numeric value paired with its {@link Unit}.
 *
 * <p>{@code Quantity} is the central class of the library. It is generic so
 * that the compiler can enforce unit compatibility: a {@code Quantity<WeightUnit>}
 * can only be converted to another {@code WeightUnit}, never to a
 * {@code DistanceUnit} or {@code TimeUnit}.
 *
 * <pre>
 *   Quantity&lt;WeightUnit&gt; flour = new Quantity&lt;&gt;(2.0, WeightUnit.POUNDS);
 *   Quantity&lt;WeightUnit&gt; inGrams = flour.convertTo(WeightUnit.GRAMS);
 *   // inGrams.getValue() ≈ 907.185
 * </pre>
 *
 * <p>Conversion always goes through the base unit of the category, so adding a
 * new unit only requires one relationship rather than a full conversion table.
 *
 * @param <U> the category of unit (e.g., {@link WeightUnit}, {@link DistanceUnit})
 */
public final class Quantity<U extends Unit> {

    private final double value;
    private final U unit;

    /**
     * Creates a quantity with the given value and unit.
     *
     * @param value the numeric magnitude
     * @param unit  the unit of measurement
     */
    public Quantity(double value, U unit) {
        if (unit == null) throw new IllegalArgumentException("Unit must not be null.");
        this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the numeric value of this quantity in its current unit.
     *
     * @return the magnitude
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the unit of this quantity.
     *
     * @return the unit
     */
    public U getUnit() {
        return unit;
    }

    /**
     * Converts this quantity to a different unit of the same category.
     *
     * <p>The conversion goes through the base unit:
     * {@code this.unit → base → target}.
     *
     * @param target the unit to convert to
     * @return a new {@code Quantity} with the equivalent value in {@code target}
     */
    public Quantity<U> convertTo(U target) {
        double inBase = unit.toBase(value);
        double converted = target.fromBase(inBase);
        return new Quantity<>(converted, target);
    }

    /**
     * Returns a human-readable string, e.g., {@code "2.00 lb"}.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return String.format("%.4f %s", value, unit.symbol());
    }
}
