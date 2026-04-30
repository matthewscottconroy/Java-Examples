package com.patterns.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Receiver — the kitchen that actually prepares the food.
 *
 * <p>In the Command pattern, the receiver knows how to perform the work.
 * The command object bridges the invoker (waiter) to the receiver (kitchen)
 * without them ever meeting directly.
 */
public class Kitchen {

    private final List<String> preparationLog = new ArrayList<>();

    /**
     * Begins preparing the named dish.
     *
     * @param dish the dish to prepare
     * @param tableNumber the table number for the ticket
     */
    public void prepare(String dish, int tableNumber) {
        String ticket = "Table " + tableNumber + " — " + dish;
        preparationLog.add(ticket);
        System.out.println("  [Kitchen] Cooking: " + ticket);
    }

    /**
     * Cancels preparation of the named dish (if not yet plated).
     *
     * @param dish the dish to cancel
     * @param tableNumber the table number
     */
    public void cancelPreparation(String dish, int tableNumber) {
        String ticket = "Table " + tableNumber + " — " + dish;
        preparationLog.remove(ticket);
        System.out.println("  [Kitchen] CANCEL:  " + ticket);
    }

    /** @return the preparation log for audit/review */
    public List<String> getPreparationLog() { return List.copyOf(preparationLog); }
}
