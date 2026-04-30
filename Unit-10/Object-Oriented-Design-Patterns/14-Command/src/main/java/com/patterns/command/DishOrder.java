package com.patterns.command;

/**
 * Concrete Command — an order for a specific dish at a specific table.
 *
 * <p>Holds the request (dish + table) and a reference to the {@link Kitchen}
 * receiver. {@link #execute()} asks the kitchen to start cooking;
 * {@link #cancel()} tells the kitchen to stop.
 */
public class DishOrder implements Order {

    private final Kitchen kitchen;
    private final String  dish;
    private final int     tableNumber;

    /**
     * @param kitchen     the kitchen that will prepare this dish
     * @param dish        the name of the dish
     * @param tableNumber the table requesting the dish
     */
    public DishOrder(Kitchen kitchen, String dish, int tableNumber) {
        this.kitchen     = kitchen;
        this.dish        = dish;
        this.tableNumber = tableNumber;
    }

    @Override
    public void execute() {
        kitchen.prepare(dish, tableNumber);
    }

    @Override
    public void cancel() {
        kitchen.cancelPreparation(dish, tableNumber);
    }

    @Override
    public String getDescription() {
        return "Table " + tableNumber + " — " + dish;
    }
}
