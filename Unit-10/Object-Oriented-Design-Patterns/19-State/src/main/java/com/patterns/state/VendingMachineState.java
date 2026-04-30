package com.patterns.state;

/**
 * State interface — every concrete state must handle the same set of events.
 *
 * <p>The vending machine delegates each user action to its current state object,
 * so adding a new state never requires touching any other state class.
 */
public interface VendingMachineState {

    /** User presses a product button. */
    void selectItem(VendingMachine machine, String itemCode);

    /** User inserts a coin or bill (amount in cents). */
    void insertMoney(VendingMachine machine, int cents);

    /** User presses the "dispense" or "confirm" button. */
    void dispense(VendingMachine machine);

    /** User presses the cancel / coin-return button. */
    void cancel(VendingMachine machine);

    /** Human-readable name for logging and display. */
    String stateName();
}
