package com.patterns.chain;

/**
 * Abstract Handler — holds the next handler in the chain and defines the
 * approval contract.
 *
 * <p>Each concrete approver has a spending limit. If the expense is within its
 * authority it approves and the chain ends. Otherwise it passes the report to
 * the next approver. If no approver can handle the request, it is rejected.
 */
public abstract class Approver {

    private Approver next;

    /**
     * Sets the next approver in the chain.
     *
     * @param next the approver to delegate to when this one cannot approve
     * @return the next approver (enables fluent chain construction)
     */
    public Approver setNext(Approver next) {
        this.next = next;
        return next;
    }

    /**
     * Attempts to approve the expense report. Approves if within authority;
     * otherwise passes to the next handler.
     *
     * @param report the expense report to process
     * @return true if the expense was approved by any handler in the chain
     */
    public final boolean approve(ExpenseReport report) {
        if (report.amountUsd() <= getLimit()) {
            System.out.printf("  ✓ Approved by %-20s (%s) — $%.2f%n",
                    getTitle(), getClass().getSimpleName(), report.amountUsd());
            return true;
        }
        if (next != null) {
            System.out.printf("  ↑ Escalating past %-16s (limit $%.0f) for $%.2f%n",
                    getTitle(), getLimit(), report.amountUsd());
            return next.approve(report);
        }
        System.out.printf("  ✗ REJECTED — $%.2f exceeds all approval limits%n",
                report.amountUsd());
        return false;
    }

    /** @return the maximum dollar amount this approver can sign off */
    protected abstract double getLimit();

    /** @return the job title of this approver */
    protected abstract String getTitle();
}
