package com.functional.lambdas;

/**
 * A functional interface representing a single tax rule.
 *
 * <p>Because this interface has exactly one abstract method, any lambda
 * of the shape {@code (double) -> double} is a valid {@code TaxRule}.
 * No anonymous class boilerplate required.
 */
@FunctionalInterface
public interface TaxRule {

    /**
     * Apply this rule to a gross income and return the tax owed.
     *
     * @param grossIncome pre-tax income in dollars
     * @return tax amount in dollars
     */
    double calculate(double grossIncome);
}
