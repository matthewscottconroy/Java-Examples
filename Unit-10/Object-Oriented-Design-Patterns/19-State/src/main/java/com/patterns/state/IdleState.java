package com.patterns.state;

/** The machine is waiting — nothing is selected, no money has been inserted. */
public class IdleState implements VendingMachineState {

    @Override
    public void selectItem(VendingMachine machine, String itemCode) {
        if (!machine.hasItem(itemCode)) {
            System.out.println("  [Idle] Item '" + itemCode + "' is unavailable or out of stock.");
            return;
        }
        machine.setSelectedItem(itemCode);
        System.out.println("  [Idle] Selected: " + itemCode
                + " (" + machine.priceCents(itemCode) + "¢). Please insert money.");
        machine.transitionTo(VendingMachine.ITEM_SELECTED);
    }

    @Override
    public void insertMoney(VendingMachine machine, int cents) {
        System.out.println("  [Idle] Please select an item before inserting money.");
    }

    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("  [Idle] Nothing to dispense — select an item first.");
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("  [Idle] Nothing to cancel.");
    }

    @Override
    public String stateName() { return "IDLE"; }
}
