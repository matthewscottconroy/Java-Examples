package com.patterns.chain;

/**
 * The request — an expense report submitted for approval.
 *
 * @param submitter   the employee who submitted the expense
 * @param description what the expense was for
 * @param amountUsd   the dollar amount of the expense
 */
public record ExpenseReport(String submitter, String description, double amountUsd) {

    @Override
    public String toString() {
        return String.format("Expense[%s, \"%s\", $%.2f]", submitter, description, amountUsd);
    }
}
