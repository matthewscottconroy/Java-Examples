package com.functional.pure;

/**
 * Demonstrates pure functions and immutability with a unit converter.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Unit Converter (Pure Functions & Immutability) ===\n");

        Quantity bodyTemp   = new Quantity(37.0, "°C");
        Quantity freezing   = new Quantity(0.0,  "°C");
        Quantity boiling    = new Quantity(100.0,"°C");
        Quantity marathon   = new Quantity(42.195, "km");
        Quantity weightKg   = new Quantity(70.0, "kg");
        Quantity speedLimit = new Quantity(100.0, "kph");

        System.out.println("--- Temperature ---");
        System.out.printf("  %s  =  %s%n", bodyTemp, UnitConverter.celsiusToFahrenheit(bodyTemp));
        System.out.printf("  %s  =  %s%n", freezing, UnitConverter.celsiusToFahrenheit(freezing));
        System.out.printf("  %s  =  %s%n", boiling,  UnitConverter.celsiusToFahrenheit(boiling));

        System.out.println("\n--- Distance ---");
        System.out.printf("  Marathon %s  =  %s%n",
                marathon, UnitConverter.kilometresToMiles(marathon));

        System.out.println("\n--- Mass ---");
        System.out.printf("  %s  =  %s%n", weightKg, UnitConverter.kilogramsToPounds(weightKg));

        System.out.println("\n--- Speed ---");
        System.out.printf("  %s  =  %s%n", speedLimit, UnitConverter.kphToMph(speedLimit));

        // Demonstrate immutability: originals are unchanged
        System.out.println("\n--- Originals unchanged after conversion ---");
        System.out.println("  bodyTemp still: " + bodyTemp);
        System.out.println("  marathon still: " + marathon);

        // Chain conversions: °C → °F → back to °C (round-trip)
        Quantity roundTrip = UnitConverter.fahrenheitToCelsius(
                             UnitConverter.celsiusToFahrenheit(bodyTemp));
        System.out.printf("%n  Round-trip 37°C → °F → °C: %s%n", roundTrip);
    }
}
