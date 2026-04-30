package com.patterns.visitor;

/**
 * Concrete visitor — produces a plain-text tax summary line for each income source.
 *
 * <p>A second visitor over the same element tree, demonstrating how new
 * operations are added without touching income classes.
 */
public class TaxSummaryVisitor implements TaxVisitor {

    private final TaxVisitor taxCalculator;

    public TaxSummaryVisitor(TaxVisitor taxCalculator) {
        this.taxCalculator = taxCalculator;
    }

    @Override
    public double visit(SalaryIncome income) {
        double tax = taxCalculator.visit(income);
        System.out.printf("  %-40s  gross $%,.0f  →  tax $%,.0f%n",
                income.description(), income.grossAmount(), tax);
        return tax;
    }

    @Override
    public double visit(DividendIncome income) {
        double tax = taxCalculator.visit(income);
        System.out.printf("  %-40s  gross $%,.0f  →  tax $%,.0f%n",
                income.description(), income.grossAmount(), tax);
        return tax;
    }

    @Override
    public double visit(CapitalGainIncome income) {
        double tax = taxCalculator.visit(income);
        System.out.printf("  %-40s  gross $%,.0f  →  tax $%,.0f%n",
                income.description(), income.grossAmount(), tax);
        return tax;
    }

    @Override
    public double visit(RentalIncome income) {
        double tax = taxCalculator.visit(income);
        System.out.printf("  %-40s  gross $%,.0f  exp $%,.0f  →  tax $%,.0f%n",
                income.description(), income.grossAmount(), income.expenses(), tax);
        return tax;
    }
}
