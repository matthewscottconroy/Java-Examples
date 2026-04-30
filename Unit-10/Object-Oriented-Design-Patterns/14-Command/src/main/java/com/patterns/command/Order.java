package com.patterns.command;

/**
 * Command interface — every restaurant order is an executable command.
 *
 * <p>Commands encapsulate a request as an object. This lets the waiter queue
 * them, the kitchen execute them, and the manager cancel them before cooking
 * begins — all without coupling the dining room to the kitchen.
 */
public interface Order {

    /**
     * Executes this order (the kitchen starts cooking the item).
     */
    void execute();

    /**
     * Undoes or cancels this order, if it has not yet been prepared.
     */
    void cancel();

    /**
     * Returns a brief description of this order for the ticket.
     *
     * @return e.g., "Table 7 — Grilled Salmon"
     */
    String getDescription();
}
