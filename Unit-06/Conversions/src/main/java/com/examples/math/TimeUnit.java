package com.examples.math;

/**
 * Units of time. The base unit is <strong>seconds</strong>.
 *
 * <p>Each constant stores a conversion factor: the number of seconds in one
 * unit. Conversion to and from any other {@code TimeUnit} goes through seconds.
 */
public enum TimeUnit implements Unit {

    /** One second — the base unit. */
    SECONDS("s", "seconds", 1.0),

    /** One minute = 60 seconds. */
    MINUTES("min", "minutes", 60.0),

    /** One hour = 3,600 seconds. */
    HOURS("hr", "hours", 3_600.0),

    /** One day = 86,400 seconds. */
    DAYS("d", "days", 86_400.0),

    /** One week = 604,800 seconds. */
    WEEKS("wk", "weeks", 604_800.0);

    private final String symbol;
    private final String fullName;
    private final double secondsPerUnit;

    TimeUnit(String symbol, String fullName, double secondsPerUnit) {
        this.symbol = symbol;
        this.fullName = fullName;
        this.secondsPerUnit = secondsPerUnit;
    }

    @Override
    public String symbol() { return symbol; }

    @Override
    public String fullName() { return fullName; }

    /** Converts {@code value} in this unit to seconds. */
    @Override
    public double toBase(double value) { return value * secondsPerUnit; }

    /** Converts {@code value} in seconds to this unit. */
    @Override
    public double fromBase(double value) { return value / secondsPerUnit; }
}
