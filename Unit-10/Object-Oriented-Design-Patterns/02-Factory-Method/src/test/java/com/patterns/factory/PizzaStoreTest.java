package com.patterns.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Factory Method pattern — Pizza Stores.
 */
class PizzaStoreTest {

    @Test
    @DisplayName("NY store produces thin-crust pizza")
    void nyStoreMakesThinCrust() {
        PizzaStore store = new NYStylePizzaStore();
        Pizza pizza = store.orderPizza("cheese");
        assertTrue(pizza.toString().contains("thin"),
                "NY style should have thin crust");
    }

    @Test
    @DisplayName("Chicago store produces thick-crust pizza")
    void chicagoStoresMakesThickCrust() {
        PizzaStore store = new ChicagoStylePizzaStore();
        Pizza pizza = store.orderPizza("cheese");
        assertTrue(pizza.toString().contains("thick"),
                "Chicago style should have thick crust");
    }

    @Test
    @DisplayName("Both stores accept the same type strings")
    void bothStoresAcceptSameTypes() {
        PizzaStore ny      = new NYStylePizzaStore();
        PizzaStore chicago = new ChicagoStylePizzaStore();

        for (String type : new String[]{"cheese", "veggie", "pepperoni"}) {
            assertDoesNotThrow(() -> ny.orderPizza(type));
            assertDoesNotThrow(() -> chicago.orderPizza(type));
        }
    }

    @Test
    @DisplayName("Unknown pizza type throws IllegalArgumentException")
    void unknownTypeThrows() {
        PizzaStore store = new NYStylePizzaStore();
        assertThrows(IllegalArgumentException.class, () -> store.orderPizza("anchovies"));
    }

    @Test
    @DisplayName("Pizza returned has a non-blank name")
    void pizzaHasName() {
        Pizza pizza = new NYStylePizzaStore().orderPizza("veggie");
        assertNotNull(pizza.getName());
        assertFalse(pizza.getName().isBlank());
    }

    @Test
    @DisplayName("NY and Chicago cheese pizzas are different objects")
    void pizzasAreDistinct() {
        Pizza ny      = new NYStylePizzaStore().orderPizza("cheese");
        Pizza chicago = new ChicagoStylePizzaStore().orderPizza("cheese");
        assertNotSame(ny, chicago);
        assertNotEquals(ny.getName(), chicago.getName());
    }
}
