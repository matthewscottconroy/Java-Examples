package com.functional.lambdas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LambdaTest {

    @Test
    @DisplayName("A bracket yields zero tax below its floor")
    void belowFloor() {
        TaxRule rule = new TaxBracket(50_000, 100_000, 0.22, "22%").toRule();
        assertEquals(0.0, rule.calculate(30_000), 0.01);
    }

    @Test
    @DisplayName("A bracket taxes only the income within its range")
    void withinBracket() {
        TaxRule rule = new TaxBracket(10_000, 40_000, 0.10, "10%").toRule();
        // income = 25_000 → taxable in bracket = 15_000 → tax = 1_500
        assertEquals(1_500.0, rule.calculate(25_000), 0.01);
    }

    @Test
    @DisplayName("A bracket caps at its ceiling")
    void aboveCeiling() {
        TaxRule rule = new TaxBracket(10_000, 40_000, 0.10, "10%").toRule();
        // income = 100_000 → taxable in bracket = 30_000 → tax = 3_000
        assertEquals(3_000.0, rule.calculate(100_000), 0.01);
    }

    @Test
    @DisplayName("TaxEngine sums multiple rules")
    void engineSumsRules() {
        TaxEngine engine = new TaxEngine()
                .addRule(new TaxBracket(0, 10_000, 0.10, "10%").toRule())
                .addRule(new TaxBracket(10_000, Double.MAX_VALUE, 0.20, "20%").toRule());
        // income = 20_000: 10% on first 10k = 1000, 20% on next 10k = 2000 → 3000
        assertEquals(3_000.0, engine.calculate(20_000), 0.01);
    }

    @Test
    @DisplayName("An inline lambda is a valid TaxRule")
    void inlineLambdaAsRule() {
        TaxRule flatTax = income -> income * 0.15;
        assertEquals(1_500.0, flatTax.calculate(10_000), 0.01);
    }

    @Test
    @DisplayName("A conditional lambda (surtax) triggers only above threshold")
    void conditionalLambda() {
        TaxRule surtax = income -> income > 100_000 ? (income - 100_000) * 0.038 : 0.0;
        assertEquals(0.0,   surtax.calculate(80_000),  0.01);
        assertEquals(380.0, surtax.calculate(110_000), 0.01);
    }
}
