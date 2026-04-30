package com.patterns.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Invoker — the waiter who collects orders and submits them to the kitchen.
 *
 * <p>The waiter does not know how dishes are prepared. They simply hold a queue
 * of {@link Order} command objects and fire them when the time is right. Orders
 * can be queued, bulk-submitted, or cancelled before submission.
 */
public class Waiter {

    private final Deque<Order>  pending   = new ArrayDeque<>();
    private final List<Order>   submitted = new ArrayList<>();

    /**
     * Adds an order to the pending queue.
     *
     * @param order the order taken at the table
     */
    public void takeOrder(Order order) {
        System.out.println("  [Waiter] Wrote down: " + order.getDescription());
        pending.add(order);
    }

    /**
     * Submits all pending orders to the kitchen in one go.
     * Clears the pending queue.
     */
    public void submitOrders() {
        System.out.println("  [Waiter] Sending " + pending.size() + " orders to kitchen...");
        while (!pending.isEmpty()) {
            Order o = pending.poll();
            o.execute();
            submitted.add(o);
        }
    }

    /**
     * Cancels the most recently taken pending order (before it reaches the kitchen).
     *
     * @return the cancelled order, or null if no pending orders
     */
    public Order cancelLastPending() {
        Order last = ((ArrayDeque<Order>) pending).peekLast();
        if (last != null) {
            ((ArrayDeque<Order>) pending).pollLast();
            System.out.println("  [Waiter] Cancelled: " + last.getDescription());
        }
        return last;
    }

    /** @return number of orders waiting to be submitted */
    public int getPendingCount()   { return pending.size(); }

    /** @return number of orders already sent to the kitchen */
    public int getSubmittedCount() { return submitted.size(); }
}
