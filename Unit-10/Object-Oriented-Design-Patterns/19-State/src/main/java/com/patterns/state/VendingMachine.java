package com.patterns.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Context — the vending machine whose behaviour changes with its state.
 *
 * <p>All public actions ({@link #selectItem}, {@link #insertMoney}, etc.) simply
 * delegate to the current {@link VendingMachineState}, which decides what is
 * legal and what transition to take next.
 */
public class VendingMachine {

    private VendingMachineState currentState;

    private final Map<String, Integer> inventory  = new HashMap<>(); // itemCode → priceCents
    private final Map<String, Integer> stock      = new HashMap<>(); // itemCode → quantity

    private String  selectedItem = null;
    private int     insertedCents = 0;
    private int     changeReturnedCents = 0; // tracks last change for testing

    // State singletons — shared because they hold no instance data
    static final VendingMachineState IDLE             = new IdleState();
    static final VendingMachineState ITEM_SELECTED    = new ItemSelectedState();
    static final VendingMachineState PAYMENT_PENDING  = new PaymentPendingState();
    static final VendingMachineState DISPENSING       = new DispensingState();

    public VendingMachine() {
        currentState = IDLE;
    }

    // ── Stock management ────────────────────────────────────────────────────

    /** Adds a product to the machine. */
    public void stock(String itemCode, int priceCents, int quantity) {
        inventory.put(itemCode, priceCents);
        stock.merge(itemCode, quantity, Integer::sum);
    }

    // ── User actions (delegate to current state) ─────────────────────────

    public void selectItem(String itemCode) { currentState.selectItem(this, itemCode); }
    public void insertMoney(int cents)      { currentState.insertMoney(this, cents); }
    public void dispense()                  { currentState.dispense(this); }
    public void cancel()                    { currentState.cancel(this); }

    // ── State transitions ────────────────────────────────────────────────

    public void transitionTo(VendingMachineState state) {
        System.out.println("  [State] " + currentState.stateName() + " → " + state.stateName());
        currentState = state;
    }

    // ── Internal helpers used by state objects ────────────────────────────

    boolean hasItem(String itemCode) {
        return inventory.containsKey(itemCode) && stock.getOrDefault(itemCode, 0) > 0;
    }

    int priceCents(String itemCode) {
        return inventory.getOrDefault(itemCode, 0);
    }

    void setSelectedItem(String code) { this.selectedItem = code; }
    String getSelectedItem()          { return selectedItem; }

    void addInsertedCents(int cents)  { this.insertedCents += cents; }
    int  getInsertedCents()           { return insertedCents; }

    void performDispense() {
        int price  = priceCents(selectedItem);
        int change = insertedCents - price;
        stock.merge(selectedItem, -1, Integer::sum);
        System.out.printf("  [Machine] Dispensing %-10s — paid %d¢, change %d¢%n",
                selectedItem, insertedCents, change);
        changeReturnedCents = change;
        insertedCents  = 0;
        selectedItem   = null;
    }

    void returnCoins() {
        if (insertedCents > 0) {
            System.out.println("  [Machine] Returning " + insertedCents + "¢");
            changeReturnedCents = insertedCents;
            insertedCents = 0;
        }
        selectedItem = null;
    }

    // ── Accessors for tests ───────────────────────────────────────────────

    public VendingMachineState getCurrentState()  { return currentState; }
    public int  getChangeReturnedCents()          { return changeReturnedCents; }
    public int  stockOf(String itemCode)          { return stock.getOrDefault(itemCode, 0); }
}
