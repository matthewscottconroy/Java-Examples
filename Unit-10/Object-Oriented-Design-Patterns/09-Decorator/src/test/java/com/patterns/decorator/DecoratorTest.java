package com.patterns.decorator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Decorator pattern — Coffee Shop.
 */
class DecoratorTest {

    @Test
    @DisplayName("Plain espresso has the base cost")
    void espressoBaseCost() {
        assertEquals(200, new Espresso().getCostCents());
    }

    @Test
    @DisplayName("Steamed milk adds 50 cents")
    void steamedMilkAddsCost() {
        Beverage latte = new SteamedMilk(new Espresso());
        assertEquals(250, latte.getCostCents());
    }

    @Test
    @DisplayName("Multiple decorators accumulate cost correctly")
    void multipleDeccoratorsCost() {
        Beverage drink = new WhipCream(new VanillaSyrup(new SteamedMilk(new Espresso())));
        // 200 + 50 + 75 + 60 = 385
        assertEquals(385, drink.getCostCents());
    }

    @Test
    @DisplayName("Description includes all condiments in order")
    void descriptionChain() {
        Beverage drink = new VanillaSyrup(new SteamedMilk(new Espresso()));
        String desc = drink.getDescription();
        assertTrue(desc.contains("Espresso"));
        assertTrue(desc.contains("Steamed Milk"));
        assertTrue(desc.contains("Vanilla Syrup"));
    }

    @Test
    @DisplayName("Same condiment can be added twice (double shot)")
    void doubleDecorator() {
        Beverage doubleShot = new ExtraShot(new ExtraShot(new Espresso()));
        // 200 + 80 + 80 = 360
        assertEquals(360, doubleShot.getCostCents());
    }

    @Test
    @DisplayName("HouseBlend with whip has correct cost")
    void houseBlendWithWhip() {
        Beverage b = new WhipCream(new HouseBlend());
        assertEquals(210, b.getCostCents()); // 150 + 60
    }
}
