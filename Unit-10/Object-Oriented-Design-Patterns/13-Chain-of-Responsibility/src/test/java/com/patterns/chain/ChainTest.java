package com.patterns.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Chain of Responsibility pattern — Expense Approval.
 */
class ChainTest {

    private Approver chain;

    @BeforeEach
    void buildChain() {
        chain = new EmployeeApprover();
        chain.setNext(new ManagerApprover())
             .setNext(new DirectorApprover())
             .setNext(new CfoApprover());
    }

    @Test
    @DisplayName("Small expense ($50) approved by Employee")
    void smallExpenseApproved() {
        assertTrue(chain.approve(new ExpenseReport("Alice", "Lunch", 50.00)));
    }

    @Test
    @DisplayName("Mid-range expense ($500) approved somewhere in chain")
    void midRangeExpenseApproved() {
        assertTrue(chain.approve(new ExpenseReport("Bob", "Hotel", 500.00)));
    }

    @Test
    @DisplayName("Large expense ($8000) approved somewhere in chain")
    void largeExpenseApproved() {
        assertTrue(chain.approve(new ExpenseReport("Carol", "Hardware", 8_000.00)));
    }

    @Test
    @DisplayName("Very large expense ($40000) approved by CFO")
    void veryLargeExpenseApprovedByCfo() {
        assertTrue(chain.approve(new ExpenseReport("Dave", "Renovation", 40_000.00)));
    }

    @Test
    @DisplayName("Chain with no CFO rejects expenses beyond Director limit")
    void chainWithoutCfoRejects() {
        Approver shortChain = new EmployeeApprover();
        shortChain.setNext(new ManagerApprover())
                  .setNext(new DirectorApprover()); // no CFO

        assertFalse(shortChain.approve(new ExpenseReport("Eve", "Huge purchase", 50_000.00)));
    }
}
