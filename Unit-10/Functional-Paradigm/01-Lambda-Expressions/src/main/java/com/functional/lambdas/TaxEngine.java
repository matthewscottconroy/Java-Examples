package com.functional.lambdas;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a list of {@link TaxRule} lambdas to compute total tax.
 *
 * <p>Rules are stored as first-class values in a {@code List<TaxRule>}.
 * Adding a new rule — a flat surcharge, a deduction, a surtax — is a
 * one-line addition of a lambda, not a new subclass.
 */
public class TaxEngine {

    private final List<TaxRule> rules = new ArrayList<>();

    /** Add any lambda that satisfies the {@code TaxRule} signature. */
    public TaxEngine addRule(TaxRule rule) {
        rules.add(rule);
        return this;
    }

    /**
     * Sum the results of every rule for the given income.
     *
     * @param grossIncome pre-tax income
     * @return total tax owed
     */
    public double calculate(double grossIncome) {
        return rules.stream()
                .mapToDouble(rule -> rule.calculate(grossIncome))
                .sum();
    }
}
