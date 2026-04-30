package com.patterns.visitor;

/** Concrete element — W-2 wages from employment. */
public record SalaryIncome(double grossAmount, String employer) implements IncomeSource {

    @Override
    public double accept(TaxVisitor visitor) { return visitor.visit(this); }

    @Override
    public String description() { return "Salary from " + employer; }
}
