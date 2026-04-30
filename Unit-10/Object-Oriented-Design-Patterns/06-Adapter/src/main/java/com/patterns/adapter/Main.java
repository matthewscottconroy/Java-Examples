package com.patterns.adapter;

/**
 * Demonstrates the Adapter pattern with a legacy payment system.
 *
 * <p>The {@link CheckoutService} expects a {@link PaymentProcessor}. The
 * legacy vendor only provides a {@link LegacyPaymentSystem}. The
 * {@link LegacyPaymentAdapter} bridges the gap — no changes to either
 * existing class are required.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Legacy Payment Adapter (Adapter Pattern) ===\n");

        // The legacy vendor's library — incompatible API, cannot be changed
        LegacyPaymentSystem legacyVendor = new LegacyPaymentSystem();

        // The adapter wraps the old system and speaks the modern interface
        PaymentProcessor adapter = new LegacyPaymentAdapter(legacyVendor);

        // The checkout service works with the modern interface — never knows it's talking to legacy
        CheckoutService checkout = new CheckoutService(adapter);

        boolean success = checkout.processOrder("CUST-1001", 4999); // $49.99
        System.out.println("Order completed: " + success);

        System.out.println();
        System.out.println("Refund test:");
        boolean refunded = adapter.refund("TXN-12345", 4999);
        System.out.println("Refund accepted: " + refunded);
    }
}
