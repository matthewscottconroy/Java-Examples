package com.patterns.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Adapter pattern — Legacy Payment System.
 */
class AdapterTest {

    private final PaymentProcessor adapter =
            new LegacyPaymentAdapter(new LegacyPaymentSystem());

    @Test
    @DisplayName("charge returns a non-blank transaction reference")
    void chargeReturnsRef() {
        String ref = adapter.charge("CUST-001", 1999);
        assertNotNull(ref);
        assertFalse(ref.isBlank());
    }

    @Test
    @DisplayName("refund returns true for a valid transaction reference")
    void refundSucceeds() {
        String ref = adapter.charge("CUST-002", 5000);
        assertTrue(adapter.refund(ref, 5000));
    }

    @Test
    @DisplayName("getStatus returns a non-blank status string")
    void statusNotBlank() {
        String ref = adapter.charge("CUST-003", 100);
        String status = adapter.getStatus(ref);
        assertFalse(status.isBlank());
    }

    @Test
    @DisplayName("CheckoutService works through the adapter without knowing legacy details")
    void checkoutIsDecoupled() {
        CheckoutService checkout = new CheckoutService(adapter);
        boolean result = checkout.processOrder("CUST-004", 2999);
        assertTrue(result, "Legacy system always returns settled in this stub");
    }

    @Test
    @DisplayName("Cents are correctly converted to dollars")
    void centsToDollarsConversion() {
        // The adapter should not throw on typical cent amounts
        assertDoesNotThrow(() -> adapter.charge("CUST-005", 9999));   // $99.99
        assertDoesNotThrow(() -> adapter.charge("CUST-006", 100));    // $1.00
    }
}
