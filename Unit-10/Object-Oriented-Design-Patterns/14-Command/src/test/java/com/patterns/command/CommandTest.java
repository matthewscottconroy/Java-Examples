package com.patterns.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Command pattern — Restaurant Order System.
 */
class CommandTest {

    @Test
    @DisplayName("execute sends the dish to the kitchen")
    void executePreparesFood() {
        Kitchen kitchen = new Kitchen();
        Order order = new DishOrder(kitchen, "Pasta", 3);
        order.execute();
        assertTrue(kitchen.getPreparationLog().contains("Table 3 — Pasta"));
    }

    @Test
    @DisplayName("cancel removes the dish from the kitchen log")
    void cancelRemovesFromLog() {
        Kitchen kitchen = new Kitchen();
        Order order = new DishOrder(kitchen, "Soup", 5);
        order.execute();
        order.cancel();
        assertFalse(kitchen.getPreparationLog().contains("Table 5 — Soup"));
    }

    @Test
    @DisplayName("Waiter queues orders without executing them immediately")
    void waiterQueuesWithoutExecuting() {
        Kitchen kitchen = new Kitchen();
        Waiter waiter = new Waiter();
        waiter.takeOrder(new DishOrder(kitchen, "Salad", 1));
        assertTrue(kitchen.getPreparationLog().isEmpty(), "Kitchen should not cook until submitOrders()");
    }

    @Test
    @DisplayName("submitOrders executes all pending commands")
    void submitOrdersExecutesAll() {
        Kitchen kitchen = new Kitchen();
        Waiter waiter = new Waiter();
        waiter.takeOrder(new DishOrder(kitchen, "Burger", 2));
        waiter.takeOrder(new DishOrder(kitchen, "Fries",  2));
        waiter.submitOrders();
        assertEquals(2, kitchen.getPreparationLog().size());
        assertEquals(0, waiter.getPendingCount());
        assertEquals(2, waiter.getSubmittedCount());
    }

    @Test
    @DisplayName("cancelLastPending removes the most recent pending order")
    void cancelLastPending() {
        Kitchen kitchen = new Kitchen();
        Waiter waiter = new Waiter();
        waiter.takeOrder(new DishOrder(kitchen, "Steak", 4));
        waiter.takeOrder(new DishOrder(kitchen, "Wine",  4));
        waiter.cancelLastPending();
        assertEquals(1, waiter.getPendingCount());
        waiter.submitOrders();
        assertFalse(kitchen.getPreparationLog().contains("Table 4 — Wine"));
    }
}
