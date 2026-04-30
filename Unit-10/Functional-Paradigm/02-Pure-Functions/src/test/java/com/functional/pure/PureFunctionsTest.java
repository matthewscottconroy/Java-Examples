package com.functional.pure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PureFunctionsTest {

    private static final double DELTA = 0.001;

    @Test
    @DisplayName("0°C converts to 32°F")
    void freezingPoint() {
        Quantity result = UnitConverter.celsiusToFahrenheit(new Quantity(0, "°C"));
        assertEquals(32.0, result.value(), DELTA);
        assertEquals("°F", result.unit());
    }

    @Test
    @DisplayName("100°C converts to 212°F")
    void boilingPoint() {
        Quantity result = UnitConverter.celsiusToFahrenheit(new Quantity(100, "°C"));
        assertEquals(212.0, result.value(), DELTA);
    }

    @Test
    @DisplayName("Celsius → Fahrenheit → Celsius is a round trip")
    void roundTrip() {
        Quantity original = new Quantity(37.0, "°C");
        Quantity back = UnitConverter.fahrenheitToCelsius(
                        UnitConverter.celsiusToFahrenheit(original));
        assertEquals(original.value(), back.value(), DELTA);
    }

    @Test
    @DisplayName("Conversion does not mutate the input")
    void inputIsImmutable() {
        Quantity input = new Quantity(100.0, "°C");
        UnitConverter.celsiusToFahrenheit(input);
        assertEquals(100.0, input.value());
        assertEquals("°C",  input.unit());
    }

    @Test
    @DisplayName("Same input always produces the same output (referential transparency)")
    void referentialTransparency() {
        Quantity q = new Quantity(25.0, "km");
        Quantity r1 = UnitConverter.kilometresToMiles(q);
        Quantity r2 = UnitConverter.kilometresToMiles(q);
        assertEquals(r1.value(), r2.value(), DELTA);
    }

    @Test
    @DisplayName("0°C is 273.15 K")
    void absoluteZeroCelsius() {
        Quantity result = UnitConverter.celsiusToKelvin(new Quantity(0, "°C"));
        assertEquals(273.15, result.value(), DELTA);
    }

    @Test
    @DisplayName("1 metre is approximately 3.281 feet")
    void metrestoFeet() {
        Quantity result = UnitConverter.metresToFeet(new Quantity(1.0, "m"));
        assertEquals(3.28084, result.value(), DELTA);
    }
}
