package com.functional.currying;

import java.util.List;
import java.util.function.Function;

/**
 * Demonstrates currying and partial application with a shipping cost calculator.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Shipping Calculator (Currying & Partial Application) ===\n");

        // Carrier rates ($/kg/km)
        final double FEDEX_RATE = 0.0045;
        final double UPS_RATE   = 0.0038;
        final double USPS_RATE  = 0.0028;

        // --- Full curried call ---
        System.out.println("1. Fully curried call:");
        double cost = ShippingCalculator.curriedCost(FEDEX_RATE).apply(5.0).apply(300.0);
        System.out.printf("   FedEx, 5 kg, 300 km → $%.2f%n%n", cost);

        // --- Partial application: fix the carrier ---
        System.out.println("2. Partial application — fix carrier, vary weight and distance:");
        Function<Double, Function<Double, Double>> fedex = ShippingCalculator.forCarrier(FEDEX_RATE);
        Function<Double, Function<Double, Double>> ups   = ShippingCalculator.forCarrier(UPS_RATE);
        Function<Double, Function<Double, Double>> usps  = ShippingCalculator.forCarrier(USPS_RATE);

        List<double[]> shipments = List.of(
                new double[]{2.0, 100.0},
                new double[]{5.0, 300.0},
                new double[]{10.0, 800.0}
        );

        System.out.printf("  %-10s %-10s  %-10s  %-10s  %-10s%n",
                "Weight", "Distance", "FedEx", "UPS", "USPS");
        for (double[] s : shipments) {
            double w = s[0], d = s[1];
            System.out.printf("  %-10.1f %-10.1f  $%-9.2f  $%-9.2f  $%-9.2f%n",
                    w, d,
                    fedex.apply(w).apply(d),
                    ups.apply(w).apply(d),
                    usps.apply(w).apply(d));
        }

        // --- Further partial: fix carrier + weight, vary only distance ---
        System.out.println("\n3. Fix carrier + weight — price by destination:");
        Function<Double, Double> heavyParcelFedEx = ShippingCalculator.forCarrierAndWeight(FEDEX_RATE, 15.0);

        List<String> destinations = List.of("Local (50 km)", "Regional (250 km)", "National (1200 km)");
        List<Double> distances    = List.of(50.0, 250.0, 1200.0);

        for (int i = 0; i < destinations.size(); i++) {
            System.out.printf("   %-22s → $%.2f%n",
                    destinations.get(i), heavyParcelFedEx.apply(distances.get(i)));
        }
    }
}
