package com.patterns.chain;

/** Approver — a manager can approve expenses up to $1,000. */
public class ManagerApprover extends Approver {
    @Override protected double getLimit() { return 1_000; }
    @Override protected String getTitle() { return "Manager"; }
}
