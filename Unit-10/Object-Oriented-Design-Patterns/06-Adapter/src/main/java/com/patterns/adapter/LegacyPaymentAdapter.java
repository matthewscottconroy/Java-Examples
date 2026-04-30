package com.patterns.adapter;

/**
 * Adapter — wraps the {@link LegacyPaymentSystem} and exposes the modern
 * {@link PaymentProcessor} interface.
 *
 * <p>The checkout service calls {@code charge()}, {@code refund()}, and
 * {@code getStatus()} — standard modern methods. The adapter translates each
 * call into the legacy system's method names, unit conventions (cents → dollars),
 * and return codes (integer → string).
 *
 * <p><b>Pattern roles:</b>
 * <pre>
 *   PaymentProcessor       — Target
 *   LegacyPaymentSystem    — Adaptee
 *   LegacyPaymentAdapter   — Adapter
 * </pre>
 */
public class LegacyPaymentAdapter implements PaymentProcessor {

    private final LegacyPaymentSystem legacy;

    /**
     * @param legacy the legacy system instance to delegate to
     */
    public LegacyPaymentAdapter(LegacyPaymentSystem legacy) {
        this.legacy = legacy;
    }

    /**
     * Translates a modern {@code charge(customerId, cents)} call into
     * the legacy {@code debitAccount(accountNumber, dollarsFloat)} call.
     */
    @Override
    public String charge(String customerId, int amountCents) {
        double dollars = amountCents / 100.0;
        return legacy.debitAccount(customerId, dollars);
    }

    /**
     * Translates a modern {@code refund(ref, cents)} call into the legacy
     * {@code creditAccount(code, dollarsFloat)} call, and maps the integer
     * return code to a boolean.
     */
    @Override
    public boolean refund(String transactionRef, int amountCents) {
        double dollars = amountCents / 100.0;
        int result = legacy.creditAccount(transactionRef, dollars);
        return result == 0; // legacy: 0 = success
    }

    /**
     * Translates the legacy integer status code to a human-readable string.
     */
    @Override
    public String getStatus(String transactionRef) {
        int code = legacy.queryTransaction(transactionRef);
        return switch (code) {
            case 1  -> "SETTLED";
            case 2  -> "PENDING";
            case 3  -> "FAILED";
            default -> "UNKNOWN";
        };
    }
}
