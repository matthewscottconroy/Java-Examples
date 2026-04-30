package com.functional.pure;

/**
 * A library of pure conversion functions.
 *
 * <p>Every method here is <strong>pure</strong>:
 * <ul>
 *   <li>Given the same input, it always returns the same output.</li>
 *   <li>It has no side effects — it does not modify state, write to disk,
 *       print to the console, or read external data.</li>
 * </ul>
 *
 * <p>Because they are pure, these functions are trivially testable, safe to
 * cache (memoize), safe to run in parallel, and easy to reason about in
 * isolation.
 */
public final class UnitConverter {

    private UnitConverter() {}

    // ── Temperature ─────────────────────────────────────────────────────

    public static Quantity celsiusToFahrenheit(Quantity celsius) {
        return new Quantity(celsius.value() * 9.0 / 5.0 + 32.0, "°F");
    }

    public static Quantity fahrenheitToCelsius(Quantity fahrenheit) {
        return new Quantity((fahrenheit.value() - 32.0) * 5.0 / 9.0, "°C");
    }

    public static Quantity celsiusToKelvin(Quantity celsius) {
        return new Quantity(celsius.value() + 273.15, "K");
    }

    // ── Length ──────────────────────────────────────────────────────────

    public static Quantity metresToFeet(Quantity metres) {
        return new Quantity(metres.value() * 3.28084, "ft");
    }

    public static Quantity feetToMetres(Quantity feet) {
        return new Quantity(feet.value() / 3.28084, "m");
    }

    public static Quantity kilometresToMiles(Quantity km) {
        return new Quantity(km.value() * 0.621371, "mi");
    }

    public static Quantity milesToKilometres(Quantity miles) {
        return new Quantity(miles.value() / 0.621371, "km");
    }

    // ── Mass ────────────────────────────────────────────────────────────

    public static Quantity kilogramsToPounds(Quantity kg) {
        return new Quantity(kg.value() * 2.20462, "lb");
    }

    public static Quantity poundsToKilograms(Quantity pounds) {
        return new Quantity(pounds.value() / 2.20462, "kg");
    }

    // ── Speed ───────────────────────────────────────────────────────────

    public static Quantity kphToMph(Quantity kph) {
        return new Quantity(kph.value() * 0.621371, "mph");
    }

    public static Quantity mphToKph(Quantity mph) {
        return new Quantity(mph.value() / 0.621371, "kph");
    }
}
