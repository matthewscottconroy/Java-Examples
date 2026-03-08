package com.examples.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Quantity")
class QuantityTest {

    private static final double DELTA = 1e-6;

    @Test
    @DisplayName("getValue and getUnit return the constructed values")
    void constructorStoresValues() {
        Quantity<WeightUnit> q = new Quantity<>(5.0, WeightUnit.KILOGRAMS);
        assertEquals(5.0, q.getValue());
        assertEquals(WeightUnit.KILOGRAMS, q.getUnit());
    }

    @Test
    @DisplayName("constructor rejects null unit")
    void constructorRejectsNullUnit() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity<>(1.0, null));
    }

    @Test
    @DisplayName("converting to the same unit returns an equal value")
    void convertToSameUnit() {
        Quantity<WeightUnit> q = new Quantity<>(3.0, WeightUnit.GRAMS);
        Quantity<WeightUnit> result = q.convertTo(WeightUnit.GRAMS);
        assertEquals(3.0, result.getValue(), DELTA);
        assertEquals(WeightUnit.GRAMS, result.getUnit());
    }

    @Test
    @DisplayName("convertTo returns a new Quantity, not the original")
    void convertToReturnsNewObject() {
        Quantity<WeightUnit> q = new Quantity<>(1.0, WeightUnit.KILOGRAMS);
        Quantity<WeightUnit> result = q.convertTo(WeightUnit.GRAMS);
        assertNotSame(q, result);
    }

    @Test
    @DisplayName("toString includes the value and symbol")
    void toStringFormat() {
        Quantity<WeightUnit> q = new Quantity<>(2.5, WeightUnit.KILOGRAMS);
        String s = q.toString();
        assertTrue(s.contains("kg"), "Expected symbol 'kg' in: " + s);
        assertTrue(s.contains("2.5"), "Expected value '2.5' in: " + s);
    }

    @Test
    @DisplayName("double conversion round-trips back to the original value")
    void roundTrip() {
        Quantity<DistanceUnit> original = new Quantity<>(100.0, DistanceUnit.METERS);
        Quantity<DistanceUnit> toMiles  = original.convertTo(DistanceUnit.MILES);
        Quantity<DistanceUnit> backToM  = toMiles.convertTo(DistanceUnit.METERS);
        assertEquals(original.getValue(), backToM.getValue(), DELTA);
    }
}
