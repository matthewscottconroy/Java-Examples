package com.patterns.visitor;

/** Concrete element — net rental income after expenses. */
public record RentalIncome(double grossAmount, double expenses, String property) implements IncomeSource {

    @Override
    public double accept(TaxVisitor visitor) { return visitor.visit(this); }

    public double netAmount() { return grossAmount - expenses; }

    @Override
    public String description() { return "Rental income from " + property; }
}
