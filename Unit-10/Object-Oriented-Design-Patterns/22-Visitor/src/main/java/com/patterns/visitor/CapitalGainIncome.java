package com.patterns.visitor;

/** Concrete element — profit from selling an asset. */
public record CapitalGainIncome(double grossAmount, boolean longTerm) implements IncomeSource {

    @Override
    public double accept(TaxVisitor visitor) { return visitor.visit(this); }

    @Override
    public String description() {
        return (longTerm ? "Long-term" : "Short-term") + " capital gain";
    }
}
