package com.patterns.command;

/**
 * Demonstrates the Command pattern with a restaurant order system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Restaurant Order System (Command Pattern) ===\n");

        Kitchen kitchen = new Kitchen();
        Waiter  waiter  = new Waiter();

        // Waiter takes orders — nothing is cooked yet
        System.out.println("--- Taking orders ---");
        waiter.takeOrder(new DishOrder(kitchen, "Grilled Salmon",   7));
        waiter.takeOrder(new DishOrder(kitchen, "Caesar Salad",     7));
        waiter.takeOrder(new DishOrder(kitchen, "Ribeye Steak",     8));
        waiter.takeOrder(new DishOrder(kitchen, "Mushroom Risotto", 8));

        // Guest at table 8 changes their mind before waiter reaches kitchen
        System.out.println("\n--- Guest at table 8 cancels last order ---");
        waiter.cancelLastPending();

        // Waiter submits all remaining orders at once
        System.out.println("\n--- Waiter submits to kitchen ---");
        waiter.submitOrders();

        System.out.println("\n--- Kitchen preparation log ---");
        kitchen.getPreparationLog().forEach(t -> System.out.println("  " + t));

        System.out.println("\nPending: " + waiter.getPendingCount()
                + " | Submitted: " + waiter.getSubmittedCount());
    }
}
