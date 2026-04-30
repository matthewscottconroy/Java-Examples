package com.patterns.chain;

/** Approver — a director can approve expenses up to $10,000. */
public class DirectorApprover extends Approver {
    @Override protected double getLimit() { return 10_000; }
    @Override protected String getTitle() { return "Director"; }
}
