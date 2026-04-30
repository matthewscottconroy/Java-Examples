package com.patterns.factory;

import java.util.List;

/**
 * Concrete Creator — New York style franchise.
 *
 * <p>Overrides {@link #createPizza(String)} to produce NY-style pizzas:
 * thin crust, tangy marinara, and generous toppings spread to the edge.
 */
public class NYStylePizzaStore extends PizzaStore {

    @Override
    protected Pizza createPizza(String type) {
        return switch (type.toLowerCase()) {
            case "cheese"    -> new NYCheesePizza();
            case "veggie"    -> new NYVeggiePizza();
            case "pepperoni" -> new NYPepperoniPizza();
            default          -> throw new IllegalArgumentException("Unknown pizza type: " + type);
        };
    }

    // -----------------------------------------------------------------------
    // Concrete products — inner classes keep them coupled to their creator
    // -----------------------------------------------------------------------

    /** Classic NY cheese pizza: thin crust, extra mozz. */
    private static class NYCheesePizza extends Pizza {
        NYCheesePizza() {
            super("NY Style Cheese Pizza", "thin, crispy",
                    "tangy marinara", List.of("reggiano cheese", "mozzarella"));
        }
    }

    /** NY veggie: paper-thin crust piled with roasted vegetables. */
    private static class NYVeggiePizza extends Pizza {
        NYVeggiePizza() {
            super("NY Style Veggie Pizza", "thin, crispy",
                    "tangy marinara",
                    List.of("roasted red peppers", "mushrooms", "onions", "garlic"));
        }
    }

    /** NY pepperoni: classic street-slice staple. */
    private static class NYPepperoniPizza extends Pizza {
        NYPepperoniPizza() {
            super("NY Style Pepperoni Pizza", "thin, crispy",
                    "tangy marinara", List.of("pepperoni", "mozzarella"));
        }
    }
}
