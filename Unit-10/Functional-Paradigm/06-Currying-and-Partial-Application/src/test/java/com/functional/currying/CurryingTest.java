package com.functional.currying;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CurryingTest {

    private static final double RATE  = 0.01;  // $0.01 / kg / km
    private static final double DELTA = 0.001;

    @Test
    @DisplayName("Fully curried call produces same result as direct call")
    void curriedMatchesDirect() {
        double curried = ShippingCalculator.curriedCost(RATE).apply(10.0).apply(200.0);
        double direct  = ShippingCalculator.cost(RATE, 10.0, 200.0);
        assertEquals(direct, curried, DELTA);
    }

    @Test
    @DisplayName("Partial application fixes the carrier rate")
    void partialCarrier() {
        Function<Double, Function<Double, Double>> pricer = ShippingCalculator.forCarrier(RATE);
        double result = pricer.apply(5.0).apply(100.0);
        assertEquals(ShippingCalculator.cost(RATE, 5.0, 100.0), result, DELTA);
    }

    @Test
    @DisplayName("forCarrierAndWeight fixes rate and weight, varies distance")
    void partialCarrierAndWeight() {
        Function<Double, Double> pricer = ShippingCalculator.forCarrierAndWeight(RATE, 8.0);
        assertEquals(ShippingCalculator.cost(RATE, 8.0, 50.0),  pricer.apply(50.0),  DELTA);
        assertEquals(ShippingCalculator.cost(RATE, 8.0, 500.0), pricer.apply(500.0), DELTA);
    }

    @Test
    @DisplayName("Two carriers with same weight and distance produce different costs")
    void differentCarriersProduceDifferentCosts() {
        Function<Double, Function<Double, Double>> cheap  = ShippingCalculator.forCarrier(0.003);
        Function<Double, Function<Double, Double>> pricey = ShippingCalculator.forCarrier(0.006);
        assertTrue(cheap.apply(5.0).apply(200.0) < pricey.apply(5.0).apply(200.0));
    }

    @Test
    @DisplayName("Cost scales linearly with weight")
    void linearInWeight() {
        double single = ShippingCalculator.cost(RATE, 1.0, 100.0);
        double double_ = ShippingCalculator.cost(RATE, 2.0, 100.0);
        assertEquals(single * 2, double_, DELTA);
    }
}
