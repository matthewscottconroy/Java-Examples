package com.patterns.visitor;

import java.util.List;

/**
 * Demonstrates the Visitor pattern with a tax return processor.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Tax Return Processor (Visitor Pattern) ===\n");

        List<IncomeSource> incomes = List.of(
                new SalaryIncome(95_000, "Acme Corp"),
                new DividendIncome(4_200, true),           // qualified
                new DividendIncome(800,   false),           // ordinary
                new CapitalGainIncome(12_000, true),        // long-term
                new CapitalGainIncome(3_000,  false),       // short-term
                new RentalIncome(18_000, 5_400, "123 Oak St")
        );

        FederalTaxVisitor  federalCalc = new FederalTaxVisitor();
        TaxSummaryVisitor  summary     = new TaxSummaryVisitor(federalCalc);

        System.out.println("Federal Tax Summary:");
        double totalTax = 0;
        for (IncomeSource income : incomes) {
            totalTax += income.accept(summary);
        }
        System.out.printf("%n  Total federal tax due: $%,.0f%n", totalTax);
    }
}
