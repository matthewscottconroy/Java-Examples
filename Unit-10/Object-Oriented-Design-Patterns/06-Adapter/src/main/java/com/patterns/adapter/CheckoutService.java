package com.patterns.adapter;

/**
 * Client — processes orders using only the modern {@link PaymentProcessor} interface.
 *
 * <p>This class has no knowledge of legacy systems, vendors, or API quirks.
 * It works identically whether it receives a real Stripe client or a
 * {@link LegacyPaymentAdapter} wrapping a 20-year-old payment vendor.
 */
public class CheckoutService {

    private final PaymentProcessor processor;

    /**
     * @param processor any implementation of the modern payment interface
     */
    public CheckoutService(PaymentProcessor processor) {
        this.processor = processor;
    }

    /**
     * Processes an order: charges the customer, then confirms the transaction status.
     *
     * @param customerId  the customer's identifier
     * @param amountCents the order total in cents
     * @return true if the charge settled successfully
     */
    public boolean processOrder(String customerId, int amountCents) {
        System.out.printf("Checkout: charging customer %s for $%.2f%n",
                customerId, amountCents / 100.0);
        String ref = processor.charge(customerId, amountCents);
        String status = processor.getStatus(ref);
        System.out.println("Checkout: transaction " + ref + " status = " + status);
        return "SETTLED".equals(status);
    }
}
