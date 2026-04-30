package com.patterns.visitor;

/** Concrete element — dividends from stock holdings. */
public record DividendIncome(double grossAmount, boolean qualified) implements IncomeSource {

    @Override
    public double accept(TaxVisitor visitor) { return visitor.visit(this); }

    @Override
    public String description() {
        return (qualified ? "Qualified" : "Ordinary") + " dividends";
    }
}
