package com.examples.math;

/**
 * Units of distance. The base unit is <strong>meters</strong>.
 *
 * <p>Each constant stores a conversion factor: the number of meters in one
 * unit. Conversion to and from any other {@code DistanceUnit} goes through meters.
 */
public enum DistanceUnit implements Unit {

    /** One millimeter = 0.001 meters. */
    MILLIMETERS("mm", "millimeters", 0.001),

    /** One centimeter = 0.01 meters. */
    CENTIMETERS("cm", "centimeters", 0.01),

    /** One meter — the base unit. */
    METERS("m", "meters", 1.0),

    /** One kilometer = 1,000 meters. */
    KILOMETERS("km", "kilometers", 1_000.0),

    /** One inch ≈ 0.0254 meters. */
    INCHES("in", "inches", 0.0254),

    /** One foot ≈ 0.3048 meters. */
    FEET("ft", "feet", 0.3048),

    /** One mile ≈ 1,609.344 meters. */
    MILES("mi", "miles", 1_609.344);

    private final String symbol;
    private final String fullName;
    private final double metersPerUnit;

    DistanceUnit(String symbol, String fullName, double metersPerUnit) {
        this.symbol = symbol;
        this.fullName = fullName;
        this.metersPerUnit = metersPerUnit;
    }

    @Override
    public String symbol() { return symbol; }

    @Override
    public String fullName() { return fullName; }

    /** Converts {@code value} in this unit to meters. */
    @Override
    public double toBase(double value) { return value * metersPerUnit; }

    /** Converts {@code value} in meters to this unit. */
    @Override
    public double fromBase(double value) { return value / metersPerUnit; }
}
