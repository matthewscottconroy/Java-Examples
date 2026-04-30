package com.patterns.decorator;

/**
 * Component — the common interface for both base beverages and condiment wrappers.
 *
 * <p>Every object in the decoration chain — a plain espresso, a latte (espresso +
 * milk), a vanilla latte (espresso + milk + vanilla syrup) — implements this
 * interface. Client code only ever calls {@link #getDescription()} and
 * {@link #getCostCents()}, regardless of how many layers of decoration exist.
 */
public interface Beverage {

    /**
     * Returns a human-readable description of this beverage including all
     * condiments that have been added.
     *
     * @return the full description
     */
    String getDescription();

    /**
     * Returns the total cost of this beverage in cents, including all added
     * condiments.
     *
     * @return cost in cents (e.g., 450 = $4.50)
     */
    int getCostCents();

    /**
     * Returns a formatted price string for display on a receipt.
     *
     * @return e.g., "$4.50"
     */
    default String formattedCost() {
        return String.format("$%.2f", getCostCents() / 100.0);
    }
}
