package com.patterns.factory;

import java.util.List;

/**
 * Concrete Creator — Chicago deep-dish franchise.
 *
 * <p>Overrides {@link #createPizza(String)} to produce Chicago-style deep-dish
 * pizzas: thick buttery crust, chunky tomato sauce on top, cheese underneath.
 */
public class ChicagoStylePizzaStore extends PizzaStore {

    @Override
    protected Pizza createPizza(String type) {
        return switch (type.toLowerCase()) {
            case "cheese"    -> new ChicagoCheesePizza();
            case "veggie"    -> new ChicagoVeggiePizza();
            case "pepperoni" -> new ChicagoPepperoniPizza();
            default          -> throw new IllegalArgumentException("Unknown pizza type: " + type);
        };
    }

    /** Chicago deep-dish cheese: extra-thick, extra-cheese, sauce on top. */
    private static class ChicagoCheesePizza extends Pizza {
        ChicagoCheesePizza() {
            super("Chicago Style Deep Dish Cheese Pizza",
                    "thick, buttery pan",
                    "chunky plum-tomato sauce (on top)",
                    List.of("mozzarella", "provolone"));
        }
        @Override public void cut() {
            System.out.println("  Cutting deep-dish into square slices");
        }
    }

    /** Chicago deep-dish veggie: hearty, knife-and-fork territory. */
    private static class ChicagoVeggiePizza extends Pizza {
        ChicagoVeggiePizza() {
            super("Chicago Style Deep Dish Veggie Pizza",
                    "thick, buttery pan",
                    "chunky plum-tomato sauce (on top)",
                    List.of("spinach", "mushrooms", "bell pepper", "mozzarella"));
        }
        @Override public void cut() {
            System.out.println("  Cutting deep-dish into square slices");
        }
    }

    /** Chicago deep-dish pepperoni: a serious commitment. */
    private static class ChicagoPepperoniPizza extends Pizza {
        ChicagoPepperoniPizza() {
            super("Chicago Style Deep Dish Pepperoni Pizza",
                    "thick, buttery pan",
                    "chunky plum-tomato sauce (on top)",
                    List.of("sliced pepperoni", "sausage", "mozzarella"));
        }
        @Override public void cut() {
            System.out.println("  Cutting deep-dish into square slices");
        }
    }
}
