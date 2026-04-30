package com.patterns.state;

/** The machine is physically dispensing the product — momentary, non-interruptible. */
public class DispensingState implements VendingMachineState {

    @Override
    public void selectItem(VendingMachine machine, String itemCode) {
        System.out.println("  [Dispensing] Please wait — dispensing in progress.");
    }

    @Override
    public void insertMoney(VendingMachine machine, int cents) {
        System.out.println("  [Dispensing] Please wait — dispensing in progress.");
    }

    @Override
    public void dispense(VendingMachine machine) {
        machine.performDispense();
        machine.transitionTo(VendingMachine.IDLE);
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("  [Dispensing] Cannot cancel — dispensing in progress.");
    }

    @Override
    public String stateName() { return "DISPENSING"; }
}
