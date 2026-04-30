package com.patterns.decorator;

/**
 * Concrete Component — a plain double espresso.
 *
 * <p>This is the innermost object in every decoration chain. Every condiment
 * wrapper ultimately delegates {@link #getCostCents()} and
 * {@link #getDescription()} down to this base drink.
 */
public class Espresso implements Beverage {

    @Override
    public String getDescription() { return "Espresso"; }

    @Override
    public int getCostCents()      { return 200; } // $2.00
}
