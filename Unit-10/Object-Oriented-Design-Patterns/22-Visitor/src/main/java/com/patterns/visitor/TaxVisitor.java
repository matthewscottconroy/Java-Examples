package com.patterns.visitor;

/**
 * Visitor interface — one {@code visit} overload per income type.
 *
 * <p>Adding a new calculation (e.g., state-level tax) means adding a new
 * {@code TaxVisitor} implementation, not editing any income class.
 */
public interface TaxVisitor {
    double visit(SalaryIncome income);
    double visit(DividendIncome income);
    double visit(CapitalGainIncome income);
    double visit(RentalIncome income);
}
