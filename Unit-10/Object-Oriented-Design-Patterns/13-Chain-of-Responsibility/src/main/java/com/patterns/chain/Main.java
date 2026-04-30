package com.patterns.chain;

/**
 * Demonstrates the Chain of Responsibility pattern with an expense approval workflow.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Expense Approval Chain (Chain of Responsibility) ===\n");

        // Build the chain: employee → manager → director → CFO
        Approver employee = new EmployeeApprover();
        employee.setNext(new ManagerApprover())
                .setNext(new DirectorApprover())
                .setNext(new CfoApprover());

        ExpenseReport[] reports = {
            new ExpenseReport("Alice",  "Team lunch",              45.00),
            new ExpenseReport("Bob",    "Conference hotel",       750.00),
            new ExpenseReport("Carol",  "Server hardware",      8_500.00),
            new ExpenseReport("Dave",   "Office renovation",   35_000.00),
        };

        for (ExpenseReport r : reports) {
            System.out.println("Submitting: " + r);
            employee.approve(r);
            System.out.println();
        }
    }
}
