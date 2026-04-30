package com.patterns.state;

/** Money is being accumulated; the machine waits for enough to cover the price. */
public class PaymentPendingState implements VendingMachineState {

    @Override
    public void selectItem(VendingMachine machine, String itemCode) {
        System.out.println("  [PaymentPending] Already selected " + machine.getSelectedItem()
                + ". Cancel to start over.");
    }

    @Override
    public void insertMoney(VendingMachine machine, int cents) {
        machine.addInsertedCents(cents);
        int price   = machine.priceCents(machine.getSelectedItem());
        int balance = machine.getInsertedCents();
        System.out.printf("  [PaymentPending] Inserted %d¢ — balance %d¢ / %d¢ needed%n",
                cents, balance, price);
    }

    @Override
    public void dispense(VendingMachine machine) {
        int price   = machine.priceCents(machine.getSelectedItem());
        int balance = machine.getInsertedCents();
        if (balance < price) {
            System.out.printf("  [PaymentPending] Insufficient — need %d¢ more.%n", price - balance);
            return;
        }
        machine.transitionTo(VendingMachine.DISPENSING);
        machine.dispense(); // trigger dispense in new state
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("  [PaymentPending] Cancelled — returning coins.");
        machine.returnCoins();
        machine.transitionTo(VendingMachine.IDLE);
    }

    @Override
    public String stateName() { return "PAYMENT_PENDING"; }
}
