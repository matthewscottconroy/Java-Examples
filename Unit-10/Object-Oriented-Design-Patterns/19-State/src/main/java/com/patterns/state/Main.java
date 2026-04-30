package com.patterns.state;

/**
 * Demonstrates the State pattern with a vending machine.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Vending Machine (State Pattern) ===\n");

        VendingMachine machine = new VendingMachine();
        machine.stock("A1", 125, 3);  // chips  — 125¢
        machine.stock("B2", 200, 2);  // water  — 200¢
        machine.stock("C3",  75, 1);  // gum    —  75¢

        // Happy path: select → insert exact change → dispense
        System.out.println("--- Scenario 1: buy chips with exact change ---");
        machine.selectItem("A1");
        machine.insertMoney(125);
        machine.dispense();

        System.out.println("\n--- Scenario 2: insert money in two steps ---");
        machine.selectItem("B2");
        machine.insertMoney(100);
        machine.insertMoney(100); // total 200¢
        machine.dispense();

        System.out.println("\n--- Scenario 3: cancel after selecting ---");
        machine.selectItem("C3");
        machine.cancel();

        System.out.println("\n--- Scenario 4: try to press dispense before selecting ---");
        machine.dispense();

        System.out.println("\n--- Scenario 5: try to cancel while dispensing ---");
        machine.selectItem("C3");
        machine.insertMoney(75);
        // manually force dispensing state for illustration
        machine.dispense();

        System.out.println("\n--- Scenario 6: select out-of-stock item ---");
        machine.selectItem("C3"); // now out of stock
    }
}
