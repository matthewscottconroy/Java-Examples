package com.patterns.factory;

/**
 * Abstract Creator — defines the order workflow and the factory method.
 *
 * <p>The key insight: {@code orderPizza()} is implemented <em>here</em> and
 * orchestrates the entire process (prepare → bake → cut → box). But it
 * delegates the creation of the actual {@link Pizza} object to the abstract
 * factory method {@link #createPizza(String)}, which each franchise overrides.
 *
 * <p>The creator code never mentions a concrete pizza class. It only works with
 * the {@code Pizza} interface. That is the whole point.
 */
public abstract class PizzaStore {

    /**
     * Takes an order, creates the right pizza for this franchise, and
     * walks it through the full preparation pipeline.
     *
     * @param type the kind of pizza requested ("cheese", "veggie", "pepperoni")
     * @return the finished, boxed pizza
     */
    public final Pizza orderPizza(String type) {
        Pizza pizza = createPizza(type);   // factory method — subclass decides what to make

        System.out.println("--- Making a " + pizza.getName() + " ---");
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
        System.out.println("Order complete: " + pizza);
        return pizza;
    }

    /**
     * The factory method — each franchise implements this to produce its own
     * style of pizza.
     *
     * <p>This is the method that makes Factory Method a pattern: the abstract
     * superclass depends on it, but never knows its concrete return type.
     *
     * @param type the pizza type requested by the customer
     * @return a {@link Pizza} appropriate for this franchise's style
     */
    protected abstract Pizza createPizza(String type);
}
