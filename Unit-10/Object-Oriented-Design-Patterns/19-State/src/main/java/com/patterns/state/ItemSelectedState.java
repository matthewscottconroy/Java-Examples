package com.patterns.state;

/** An item has been chosen; the machine is waiting for payment to begin. */
public class ItemSelectedState implements VendingMachineState {

    @Override
    public void selectItem(VendingMachine machine, String itemCode) {
        System.out.println("  [ItemSelected] Already selected " + machine.getSelectedItem()
                + ". Cancel first to choose a different item.");
    }

    @Override
    public void insertMoney(VendingMachine machine, int cents) {
        machine.addInsertedCents(cents);
        int price   = machine.priceCents(machine.getSelectedItem());
        int balance = machine.getInsertedCents();
        System.out.printf("  [ItemSelected] Inserted %d¢ — balance %d¢ / %d¢ needed%n",
                cents, balance, price);
        machine.transitionTo(VendingMachine.PAYMENT_PENDING);
    }

    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("  [ItemSelected] Insert money before dispensing.");
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("  [ItemSelected] Selection cancelled.");
        machine.setSelectedItem(null);
        machine.transitionTo(VendingMachine.IDLE);
    }

    @Override
    public String stateName() { return "ITEM_SELECTED"; }
}
