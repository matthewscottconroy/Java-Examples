package com.patterns.chain;

/** Approver — the CFO can approve any expense (no upper limit). */
public class CfoApprover extends Approver {
    @Override protected double getLimit() { return Double.MAX_VALUE; }
    @Override protected String getTitle() { return "CFO"; }
}
