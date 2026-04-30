package com.functional.lambdas;

/**
 * A tax bracket defined by a floor, ceiling, and marginal rate.
 *
 * <p>The bracket itself is a record (immutable data), and it exposes a
 * {@link #toRule()} method that returns a lambda — a {@link TaxRule} —
 * capturing the bracket's fields in its closure.
 *
 * @param floor  income above this threshold is taxed at {@code rate}
 * @param ceiling income above this is handled by a higher bracket (use MAX_VALUE for the top)
 * @param rate   marginal rate as a fraction, e.g. 0.22 for 22 %
 * @param label  human-readable bracket name
 */
public record TaxBracket(double floor, double ceiling, double rate, String label) {

    /**
     * Return a {@link TaxRule} lambda that calculates the tax owed within
     * this bracket only.
     */
    public TaxRule toRule() {
        return grossIncome -> {
            if (grossIncome <= floor) return 0.0;
            double taxableInBracket = Math.min(grossIncome, ceiling) - floor;
            return taxableInBracket * rate;
        };
    }
}
