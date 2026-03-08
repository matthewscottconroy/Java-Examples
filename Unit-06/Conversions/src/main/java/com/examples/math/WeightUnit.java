package com.examples.math;

/**
 * Units of weight. The base unit is <strong>grams</strong>.
 *
 * <p>Each constant stores a conversion factor: the number of grams in one
 * unit. Conversion to and from any other {@code WeightUnit} goes through grams.
 */
public enum WeightUnit implements Unit {

    /** One gram — the base unit. */
    GRAMS("g", "grams", 1.0),

    /** One kilogram = 1,000 grams. */
    KILOGRAMS("kg", "kilograms", 1_000.0),

    /** One metric ton = 1,000,000 grams. */
    METRIC_TONS("t", "metric tons", 1_000_000.0),

    /** One ounce ≈ 28.3495 grams. */
    OUNCES("oz", "ounces", 28.3495),

    /** One pound ≈ 453.592 grams. */
    POUNDS("lb", "pounds", 453.592);

    private final String symbol;
    private final String fullName;
    private final double gramsPerUnit;

    WeightUnit(String symbol, String fullName, double gramsPerUnit) {
        this.symbol = symbol;
        this.fullName = fullName;
        this.gramsPerUnit = gramsPerUnit;
    }

    @Override
    public String symbol() { return symbol; }

    @Override
    public String fullName() { return fullName; }

    /** Converts {@code value} in this unit to grams. */
    @Override
    public double toBase(double value) { return value * gramsPerUnit; }

    /** Converts {@code value} in grams to this unit. */
    @Override
    public double fromBase(double value) { return value / gramsPerUnit; }
}
