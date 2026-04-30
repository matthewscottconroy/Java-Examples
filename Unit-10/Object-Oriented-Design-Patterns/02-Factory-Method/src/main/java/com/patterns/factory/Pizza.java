package com.patterns.factory;

import java.util.List;

/**
 * The product — a pizza produced by a {@link PizzaStore}.
 *
 * <p>In the Factory Method pattern this is the <em>Product</em> type. The
 * abstract creator ({@link PizzaStore}) always returns a {@code Pizza}; it
 * never names the concrete subclass (NYStyleCheesePizza, etc.).
 */
public abstract class Pizza {

    protected final String name;
    protected final String crust;
    protected final String sauce;
    protected final List<String> toppings;

    /**
     * @param name     human-readable name shown on the receipt
     * @param crust    crust style (thin, thick, stuffed, etc.)
     * @param sauce    sauce type
     * @param toppings list of toppings
     */
    protected Pizza(String name, String crust, String sauce, List<String> toppings) {
        this.name     = name;
        this.crust    = crust;
        this.sauce    = sauce;
        this.toppings = List.copyOf(toppings);
    }

    /**
     * Prepares the pizza (slice cheese, arrange toppings, etc.).
     * Shared preparation logic — the same regardless of style.
     */
    public void prepare() {
        System.out.println("  Preparing " + name);
        System.out.println("  Tossing " + crust + " crust");
        System.out.println("  Spreading " + sauce);
        toppings.forEach(t -> System.out.println("  Adding " + t));
    }

    /** Bakes the pizza. */
    public void bake()    { System.out.println("  Baking at 375°F for 25 minutes"); }

    /** Cuts the pizza. */
    public void cut()     { System.out.println("  Cutting the pizza into slices"); }

    /** Boxes the pizza. */
    public void box()     { System.out.println("  Placing pizza in official box"); }

    /** @return the pizza's display name */
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + " [" + crust + " crust, " + sauce + "]";
    }
}
