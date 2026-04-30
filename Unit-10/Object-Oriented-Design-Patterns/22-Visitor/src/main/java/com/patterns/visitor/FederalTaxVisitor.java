package com.patterns.visitor;

/**
 * Concrete visitor — simplified US federal income tax calculation.
 *
 * <p>Rates are illustrative, not authoritative. The point is that each
 * income type is taxed differently and the logic lives here, not in the
 * income classes.
 */
public class FederalTaxVisitor implements TaxVisitor {

    private static final double SALARY_RATE           = 0.22;
    private static final double ORDINARY_DIVIDEND_RATE = 0.22;
    private static final double QUALIFIED_DIVIDEND_RATE = 0.15;
    private static final double SHORT_TERM_CG_RATE    = 0.22;
    private static final double LONG_TERM_CG_RATE     = 0.15;
    private static final double RENTAL_RATE           = 0.22;

    @Override
    public double visit(SalaryIncome income) {
        return income.grossAmount() * SALARY_RATE;
    }

    @Override
    public double visit(DividendIncome income) {
        double rate = income.qualified() ? QUALIFIED_DIVIDEND_RATE : ORDINARY_DIVIDEND_RATE;
        return income.grossAmount() * rate;
    }

    @Override
    public double visit(CapitalGainIncome income) {
        double rate = income.longTerm() ? LONG_TERM_CG_RATE : SHORT_TERM_CG_RATE;
        return income.grossAmount() * rate;
    }

    @Override
    public double visit(RentalIncome income) {
        return income.netAmount() * RENTAL_RATE;
    }
}
