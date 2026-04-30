package com.functional.pure;

/**
 * An immutable measurement: a numeric value paired with its unit.
 *
 * <p>Every method that "changes" a quantity returns a new {@code Quantity}.
 * The original is never modified. This makes quantities safe to share across
 * threads and to use as map keys or in collections without defensive copying.
 *
 * @param value the numeric magnitude
 * @param unit  the unit of measurement
 */
public record Quantity(double value, String unit) {

    @Override
    public String toString() {
        return String.format("%.4f %s", value, unit);
    }
}
