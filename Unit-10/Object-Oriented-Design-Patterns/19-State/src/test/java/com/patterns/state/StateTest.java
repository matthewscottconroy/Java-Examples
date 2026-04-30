package com.patterns.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateTest {

    private VendingMachine machine;

    @BeforeEach
    void setUp() {
        machine = new VendingMachine();
        machine.stock("A1", 100, 2); // 100¢, qty 2
        machine.stock("B2", 150, 1); // 150¢, qty 1
    }

    @Test
    @DisplayName("Machine starts in IDLE state")
    void startsIdle() {
        assertSame(VendingMachine.IDLE, machine.getCurrentState());
    }

    @Test
    @DisplayName("Selecting a valid item transitions to ITEM_SELECTED")
    void selectValidItem() {
        machine.selectItem("A1");
        assertSame(VendingMachine.ITEM_SELECTED, machine.getCurrentState());
    }

    @Test
    @DisplayName("Selecting an unknown item stays IDLE")
    void selectUnknownItem() {
        machine.selectItem("ZZ");
        assertSame(VendingMachine.IDLE, machine.getCurrentState());
    }

    @Test
    @DisplayName("Inserting money after selection transitions to PAYMENT_PENDING")
    void insertMoneyTransitionsToPaymentPending() {
        machine.selectItem("A1");
        machine.insertMoney(50);
        assertSame(VendingMachine.PAYMENT_PENDING, machine.getCurrentState());
    }

    @Test
    @DisplayName("Cancelling in ITEM_SELECTED returns to IDLE")
    void cancelInItemSelected() {
        machine.selectItem("A1");
        machine.cancel();
        assertSame(VendingMachine.IDLE, machine.getCurrentState());
    }

    @Test
    @DisplayName("Cancelling in PAYMENT_PENDING returns inserted money and goes IDLE")
    void cancelInPaymentPending() {
        machine.selectItem("A1");
        machine.insertMoney(60);
        machine.cancel();
        assertSame(VendingMachine.IDLE, machine.getCurrentState());
        assertEquals(60, machine.getChangeReturnedCents());
    }

    @Test
    @DisplayName("Dispensing with insufficient funds stays PAYMENT_PENDING")
    void insufficientFundsStaysPending() {
        machine.selectItem("A1");
        machine.insertMoney(50); // need 100¢
        machine.dispense();
        assertSame(VendingMachine.PAYMENT_PENDING, machine.getCurrentState());
    }

    @Test
    @DisplayName("Full happy path: select → pay → dispense → back to IDLE")
    void happyPath() {
        machine.selectItem("A1");
        machine.insertMoney(100);
        machine.dispense();
        assertSame(VendingMachine.IDLE, machine.getCurrentState());
        assertEquals(1, machine.stockOf("A1")); // one unit consumed
    }

    @Test
    @DisplayName("Correct change is returned after overpayment")
    void changeReturned() {
        machine.selectItem("A1"); // 100¢
        machine.insertMoney(150); // overpay by 50¢
        machine.dispense();
        assertEquals(50, machine.getChangeReturnedCents());
    }

    @Test
    @DisplayName("Inserting money in IDLE prints a message and stays IDLE")
    void insertMoneyWhileIdle() {
        machine.insertMoney(100);
        assertSame(VendingMachine.IDLE, machine.getCurrentState());
    }
}
