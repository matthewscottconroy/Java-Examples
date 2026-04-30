package com.functional.lambdas;

/**
 * Demonstrates lambda expressions as first-class values in a tax rule engine.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Tax Rule Engine (Lambda Expressions) ===\n");

        // Simplified US-style marginal brackets (illustrative, not authoritative)
        TaxBracket bracket10 = new TaxBracket(         0,  11_600, 0.10, "10%");
        TaxBracket bracket12 = new TaxBracket(    11_600,  47_150, 0.12, "12%");
        TaxBracket bracket22 = new TaxBracket(    47_150, 100_525, 0.22, "22%");
        TaxBracket bracket24 = new TaxBracket(   100_525, 191_950, 0.24, "24%");
        TaxBracket bracket32 = new TaxBracket(   191_950, 243_725, 0.32, "32%");
        TaxBracket bracket35 = new TaxBracket(   243_725, 609_350, 0.35, "35%");
        TaxBracket bracket37 = new TaxBracket(   609_350, Double.MAX_VALUE, 0.37, "37%");

        // Each bracket converts itself into a TaxRule lambda
        TaxEngine federal = new TaxEngine()
                .addRule(bracket10.toRule())
                .addRule(bracket12.toRule())
                .addRule(bracket22.toRule())
                .addRule(bracket24.toRule())
                .addRule(bracket32.toRule())
                .addRule(bracket35.toRule())
                .addRule(bracket37.toRule());

        // Add a flat Medicare surtax as an inline lambda — no class needed
        TaxEngine withSurtax = new TaxEngine()
                .addRule(bracket10.toRule())
                .addRule(bracket12.toRule())
                .addRule(bracket22.toRule())
                .addRule(bracket24.toRule())
                .addRule(bracket32.toRule())
                .addRule(bracket35.toRule())
                .addRule(bracket37.toRule())
                .addRule(income -> income > 200_000 ? (income - 200_000) * 0.038 : 0.0);

        double[] incomes = { 30_000, 75_000, 150_000, 300_000, 700_000 };

        System.out.printf("%-15s  %-15s  %-15s  %-10s%n",
                "Gross", "Federal Tax", "With Surtax", "Eff. Rate");
        System.out.println("-".repeat(60));

        for (double income : incomes) {
            double fed     = federal.calculate(income);
            double surtax  = withSurtax.calculate(income);
            double effRate = fed / income * 100;
            System.out.printf("$%-14,.0f  $%-14,.2f  $%-14,.2f  %.1f%%%n",
                    income, fed, surtax, effRate);
        }
    }
}
