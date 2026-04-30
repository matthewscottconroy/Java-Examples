package com.patterns.chain;

/** Approver — an employee can self-approve expenses up to $100. */
public class EmployeeApprover extends Approver {
    @Override protected double getLimit() { return 100; }
    @Override protected String getTitle() { return "Employee"; }
}
