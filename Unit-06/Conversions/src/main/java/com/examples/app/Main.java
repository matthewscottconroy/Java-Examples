package com.examples.app;

import com.examples.format.ConversionPrinter;
import com.examples.math.DistanceUnit;
import com.examples.math.Quantity;
import com.examples.math.TimeUnit;
import com.examples.math.WeightUnit;

import java.util.List;

/**
 * Demonstrates the unit conversion library with three everyday examples.
 *
 * <ul>
 *   <li>A marathon — distance conversions</li>
 *   <li>A bag of flour — weight conversions</li>
 *   <li>A work day — time conversions</li>
 * </ul>
 */
public final class Main {

    private Main() {}

    /** Entry point. */
    public static void main(String[] args) {
        printBanner();

        // --- Distance ---
        ConversionPrinter.printHeader("Distance");

        ConversionPrinter.printTable(
                "A marathon (26.2188 mi)",
                new Quantity<>(26.2188, DistanceUnit.MILES),
                List.of(DistanceUnit.values()));

        ConversionPrinter.printTable(
                "Mount Everest summit (8,849 m)",
                new Quantity<>(8_849.0, DistanceUnit.METERS),
                List.of(DistanceUnit.values()));

        // --- Weight ---
        ConversionPrinter.printHeader("Weight");

        ConversionPrinter.printTable(
                "A bag of flour (5 lb)",
                new Quantity<>(5.0, WeightUnit.POUNDS),
                List.of(WeightUnit.values()));

        ConversionPrinter.printTable(
                "A gold bar (1 kg)",
                new Quantity<>(1.0, WeightUnit.KILOGRAMS),
                List.of(WeightUnit.values()));

        // --- Time ---
        ConversionPrinter.printHeader("Time");

        ConversionPrinter.printTable(
                "A work day (8 hr)",
                new Quantity<>(8.0, TimeUnit.HOURS),
                List.of(TimeUnit.values()));

        ConversionPrinter.printTable(
                "One year (365 d)",
                new Quantity<>(365.0, TimeUnit.DAYS),
                List.of(TimeUnit.values()));

        System.out.println();
    }

    private static void printBanner() {
        String bold    = "\u001B[1m";
        String magenta = "\u001B[95m";
        String reset   = "\u001B[0m";
        System.out.println();
        System.out.println(bold + magenta + "  ╔══════════════════════════════════════╗" + reset);
        System.out.println(bold + magenta + "  ║       UNIT CONVERSION LIBRARY       ║" + reset);
        System.out.println(bold + magenta + "  ║   Weights, Distances, and Time      ║" + reset);
        System.out.println(bold + magenta + "  ╚══════════════════════════════════════╝" + reset);
    }
}
