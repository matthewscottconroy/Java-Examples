package com.functional.currying;

import java.util.function.Function;

/**
 * Shipping cost calculator demonstrating currying and partial application.
 *
 * <p><strong>Currying</strong> transforms a function of n arguments into a chain
 * of n one-argument functions: {@code f(a, b, c)} becomes {@code f(a)(b)(c)}.
 *
 * <p><strong>Partial application</strong> fixes some arguments upfront and
 * returns a new function that accepts the rest. Unlike currying, partial
 * application doesn't have to fix arguments one at a time.
 *
 * <p>Both techniques let you bake shared context into a function once and
 * reuse the specialised version many times.
 */
public final class ShippingCalculator {

    private ShippingCalculator() {}

    /**
     * Curried shipping cost: carrier → weight (kg) → distance (km) → cost ($).
     *
     * <p>Each call fixes one more variable and returns a function that accepts
     * the next. The final application returns a {@code Double}.
     *
     * @param ratePerKgPerKm carrier's base rate in dollars per kg per km
     */
    public static Function<Double, Function<Double, Double>> curriedCost(double ratePerKgPerKm) {
        return weightKg -> distanceKm -> weightKg * distanceKm * ratePerKgPerKm;
    }

    /**
     * Partially apply a carrier rate to produce a weight-and-distance pricer.
     *
     * <p>This is partial application: one argument fixed, two remaining.
     */
    public static Function<Double, Function<Double, Double>> forCarrier(double ratePerKgPerKm) {
        return curriedCost(ratePerKgPerKm);
    }

    /**
     * Partially apply both carrier rate and weight to produce a distance pricer.
     *
     * <p>Useful for a single heavy parcel going to multiple destinations.
     */
    public static Function<Double, Double> forCarrierAndWeight(double ratePerKgPerKm,
                                                                double weightKg) {
        return curriedCost(ratePerKgPerKm).apply(weightKg);
    }

    /**
     * Non-curried reference implementation for comparison.
     */
    public static double cost(double ratePerKgPerKm, double weightKg, double distanceKm) {
        return weightKg * distanceKm * ratePerKgPerKm;
    }
}
