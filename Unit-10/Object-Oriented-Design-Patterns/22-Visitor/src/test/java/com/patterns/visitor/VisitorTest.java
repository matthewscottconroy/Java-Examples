package com.patterns.visitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisitorTest {

    private FederalTaxVisitor visitor;

    @BeforeEach
    void setUp() {
        visitor = new FederalTaxVisitor();
    }

    @Test
    @DisplayName("Salary taxed at 22%")
    void salaryTax() {
        double tax = new SalaryIncome(100_000, "Acme").accept(visitor);
        assertEquals(22_000, tax, 0.01);
    }

    @Test
    @DisplayName("Qualified dividends taxed at 15%")
    void qualifiedDividendTax() {
        double tax = new DividendIncome(10_000, true).accept(visitor);
        assertEquals(1_500, tax, 0.01);
    }

    @Test
    @DisplayName("Ordinary dividends taxed at 22%")
    void ordinaryDividendTax() {
        double tax = new DividendIncome(10_000, false).accept(visitor);
        assertEquals(2_200, tax, 0.01);
    }

    @Test
    @DisplayName("Long-term capital gains taxed at 15%")
    void longTermCapGainTax() {
        double tax = new CapitalGainIncome(20_000, true).accept(visitor);
        assertEquals(3_000, tax, 0.01);
    }

    @Test
    @DisplayName("Short-term capital gains taxed at 22%")
    void shortTermCapGainTax() {
        double tax = new CapitalGainIncome(20_000, false).accept(visitor);
        assertEquals(4_400, tax, 0.01);
    }

    @Test
    @DisplayName("Rental income taxed on net (gross minus expenses) at 22%")
    void rentalTaxOnNet() {
        RentalIncome rental = new RentalIncome(20_000, 5_000, "Oak St");
        double tax = rental.accept(visitor);
        assertEquals(15_000 * 0.22, tax, 0.01);
    }

    @Test
    @DisplayName("Same elements can be visited by a different visitor")
    void secondVisitorProducesDifferentResult() {
        IncomeSource salary = new SalaryIncome(100_000, "Corp");
        double federalTax = salary.accept(new FederalTaxVisitor());
        // A visitor that applies a flat 10% would yield a different result
        TaxVisitor flat10 = new TaxVisitor() {
            public double visit(SalaryIncome i)      { return i.grossAmount() * 0.10; }
            public double visit(DividendIncome i)    { return i.grossAmount() * 0.10; }
            public double visit(CapitalGainIncome i) { return i.grossAmount() * 0.10; }
            public double visit(RentalIncome i)      { return i.netAmount()   * 0.10; }
        };
        double flat10Tax = salary.accept(flat10);
        assertNotEquals(federalTax, flat10Tax);
        assertEquals(10_000, flat10Tax, 0.01);
    }
}
