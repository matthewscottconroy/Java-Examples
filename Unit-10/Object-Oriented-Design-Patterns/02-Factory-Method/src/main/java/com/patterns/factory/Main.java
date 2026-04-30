package com.patterns.factory;

/**
 * Demonstrates the Factory Method pattern with two pizza franchise styles.
 *
 * <p>The ordering code is identical for both stores — {@code store.orderPizza("cheese")}.
 * The stores produce completely different pizzas, but the client code never
 * references a concrete pizza class. The factory method handles the difference.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== The Pizza Franchise (Factory Method Pattern) ===\n");

        PizzaStore nyStore       = new NYStylePizzaStore();
        PizzaStore chicagoStore  = new ChicagoStylePizzaStore();

        // Same call, two completely different pizzas
        System.out.println(">>> Order 1: cheese pizza from New York");
        Pizza nyPizza = nyStore.orderPizza("cheese");

        System.out.println("\n>>> Order 2: cheese pizza from Chicago");
        Pizza chicagoPizza = chicagoStore.orderPizza("cheese");

        System.out.println("\n>>> Order 3: veggie from NY, pepperoni from Chicago");
        nyStore.orderPizza("veggie");
        System.out.println();
        chicagoStore.orderPizza("pepperoni");

        System.out.println("\n--- Summary ---");
        System.out.println("NY made:      " + nyPizza);
        System.out.println("Chicago made: " + chicagoPizza);
        System.out.println("\nSame type requested, very different pizza produced.");
        System.out.println("The client code never mentioned NYCheesePizza or ChicagoCheesePizza.");
    }
}
