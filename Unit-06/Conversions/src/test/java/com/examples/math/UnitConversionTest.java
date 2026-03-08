package com.examples.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Conversions")
class UnitConversionTest {

    private static final double DELTA = 1e-4;

    // --- WeightUnit ---

    @Test
    @DisplayName("1 kilogram = 1000 grams")
    void kilogramsToGrams() {
        Quantity<WeightUnit> q = new Quantity<>(1.0, WeightUnit.KILOGRAMS);
        assertEquals(1000.0, q.convertTo(WeightUnit.GRAMS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 pound ≈ 453.592 grams")
    void poundsToGrams() {
        Quantity<WeightUnit> q = new Quantity<>(1.0, WeightUnit.POUNDS);
        assertEquals(453.592, q.convertTo(WeightUnit.GRAMS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 pound ≈ 16 ounces")
    void poundsToOunces() {
        Quantity<WeightUnit> q = new Quantity<>(1.0, WeightUnit.POUNDS);
        assertEquals(16.0, q.convertTo(WeightUnit.OUNCES).getValue(), 0.001);
    }

    @Test
    @DisplayName("1 metric ton = 1000 kilograms")
    void metricTonToKilograms() {
        Quantity<WeightUnit> q = new Quantity<>(1.0, WeightUnit.METRIC_TONS);
        assertEquals(1000.0, q.convertTo(WeightUnit.KILOGRAMS).getValue(), DELTA);
    }

    // --- DistanceUnit ---

    @Test
    @DisplayName("1 kilometer = 1000 meters")
    void kilometersToMeters() {
        Quantity<DistanceUnit> q = new Quantity<>(1.0, DistanceUnit.KILOMETERS);
        assertEquals(1000.0, q.convertTo(DistanceUnit.METERS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 mile ≈ 1609.344 meters")
    void milesToMeters() {
        Quantity<DistanceUnit> q = new Quantity<>(1.0, DistanceUnit.MILES);
        assertEquals(1609.344, q.convertTo(DistanceUnit.METERS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 foot = 12 inches")
    void feetToInches() {
        Quantity<DistanceUnit> q = new Quantity<>(1.0, DistanceUnit.FEET);
        assertEquals(12.0, q.convertTo(DistanceUnit.INCHES).getValue(), 0.001);
    }

    @Test
    @DisplayName("1 meter = 100 centimeters")
    void metersToCentimeters() {
        Quantity<DistanceUnit> q = new Quantity<>(1.0, DistanceUnit.METERS);
        assertEquals(100.0, q.convertTo(DistanceUnit.CENTIMETERS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 meter = 1000 millimeters")
    void metersToMillimeters() {
        Quantity<DistanceUnit> q = new Quantity<>(1.0, DistanceUnit.METERS);
        assertEquals(1000.0, q.convertTo(DistanceUnit.MILLIMETERS).getValue(), DELTA);
    }

    // --- TimeUnit ---

    @Test
    @DisplayName("1 minute = 60 seconds")
    void minutesToSeconds() {
        Quantity<TimeUnit> q = new Quantity<>(1.0, TimeUnit.MINUTES);
        assertEquals(60.0, q.convertTo(TimeUnit.SECONDS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 hour = 60 minutes")
    void hoursToMinutes() {
        Quantity<TimeUnit> q = new Quantity<>(1.0, TimeUnit.HOURS);
        assertEquals(60.0, q.convertTo(TimeUnit.MINUTES).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 day = 24 hours")
    void daysToHours() {
        Quantity<TimeUnit> q = new Quantity<>(1.0, TimeUnit.DAYS);
        assertEquals(24.0, q.convertTo(TimeUnit.HOURS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 week = 7 days")
    void weeksToDays() {
        Quantity<TimeUnit> q = new Quantity<>(1.0, TimeUnit.WEEKS);
        assertEquals(7.0, q.convertTo(TimeUnit.DAYS).getValue(), DELTA);
    }

    @Test
    @DisplayName("1 day = 86400 seconds")
    void daysToSeconds() {
        Quantity<TimeUnit> q = new Quantity<>(1.0, TimeUnit.DAYS);
        assertEquals(86_400.0, q.convertTo(TimeUnit.SECONDS).getValue(), DELTA);
    }

    // --- Unit metadata ---

    @Test
    @DisplayName("WeightUnit symbols are correct")
    void weightUnitSymbols() {
        assertEquals("g",  WeightUnit.GRAMS.symbol());
        assertEquals("kg", WeightUnit.KILOGRAMS.symbol());
        assertEquals("lb", WeightUnit.POUNDS.symbol());
        assertEquals("oz", WeightUnit.OUNCES.symbol());
        assertEquals("t",  WeightUnit.METRIC_TONS.symbol());
    }

    @Test
    @DisplayName("DistanceUnit full names are correct")
    void distanceUnitFullNames() {
        assertEquals("meters",      DistanceUnit.METERS.fullName());
        assertEquals("kilometers",  DistanceUnit.KILOMETERS.fullName());
        assertEquals("miles",       DistanceUnit.MILES.fullName());
        assertEquals("feet",        DistanceUnit.FEET.fullName());
    }

    @Test
    @DisplayName("TimeUnit full names are correct")
    void timeUnitFullNames() {
        assertEquals("seconds", TimeUnit.SECONDS.fullName());
        assertEquals("minutes", TimeUnit.MINUTES.fullName());
        assertEquals("hours",   TimeUnit.HOURS.fullName());
        assertEquals("days",    TimeUnit.DAYS.fullName());
        assertEquals("weeks",   TimeUnit.WEEKS.fullName());
    }
}
