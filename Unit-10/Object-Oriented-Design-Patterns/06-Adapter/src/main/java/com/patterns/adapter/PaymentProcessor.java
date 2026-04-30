package com.patterns.adapter;

/**
 * Target interface — the modern API that the checkout service expects.
 *
 * <p>Every payment processor the checkout system works with must implement
 * this interface. The checkout service never knows whether it is talking to
 * Stripe, PayPal, or a twenty-year-old legacy vendor.
 */
public interface PaymentProcessor {

    /**
     * Charges the given amount to the customer's account.
     *
     * @param customerId the customer identifier
     * @param amountCents the amount to charge in cents (e.g. 1999 = $19.99)
     * @return a transaction reference string
     */
    String charge(String customerId, int amountCents);

    /**
     * Refunds a previous transaction.
     *
     * @param transactionRef the reference returned by {@link #charge}
     * @param amountCents    the amount to refund in cents
     * @return true if the refund was accepted
     */
    boolean refund(String transactionRef, int amountCents);

    /**
     * Returns the current status of a transaction.
     *
     * @param transactionRef the transaction reference
     * @return a status string (e.g. "SETTLED", "PENDING", "FAILED")
     */
    String getStatus(String transactionRef);
}
