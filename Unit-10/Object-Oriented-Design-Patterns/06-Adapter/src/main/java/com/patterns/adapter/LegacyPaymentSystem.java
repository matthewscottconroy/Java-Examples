package com.patterns.adapter;

/**
 * Adaptee — a legacy payment system with an incompatible API.
 *
 * <p>This class was written 20 years ago by a vendor who is no longer in
 * business. The source code is available but the API cannot be changed because
 * other systems also depend on it. Its method signatures and semantics differ
 * entirely from the modern {@link PaymentProcessor} interface.
 */
public class LegacyPaymentSystem {

    /**
     * Debits an account using the legacy protocol.
     *
     * @param accountNumber the legacy account number
     * @param dollarsFloat  the amount in dollars as a floating-point number
     * @return a legacy confirmation code (e.g., "TXN-00042")
     */
    public String debitAccount(String accountNumber, double dollarsFloat) {
        System.out.println("[Legacy] Debiting account " + accountNumber
                + " for $" + String.format("%.2f", dollarsFloat));
        return "TXN-" + Math.abs(accountNumber.hashCode() % 100000);
    }

    /**
     * Issues a credit on a previous debit using the legacy protocol.
     *
     * @param legacyConfirmationCode the code returned by {@link #debitAccount}
     * @param dollarsFloat           the amount to credit
     * @return 0 = success, non-zero = failure code
     */
    public int creditAccount(String legacyConfirmationCode, double dollarsFloat) {
        System.out.println("[Legacy] Crediting " + legacyConfirmationCode
                + " for $" + String.format("%.2f", dollarsFloat));
        return 0; // 0 means success in the legacy system
    }

    /**
     * Queries a transaction using the legacy status codes.
     *
     * @param legacyConfirmationCode the code to query
     * @return an integer status (1 = settled, 2 = pending, 3 = failed)
     */
    public int queryTransaction(String legacyConfirmationCode) {
        System.out.println("[Legacy] Querying " + legacyConfirmationCode);
        return 1; // 1 = settled
    }
}
