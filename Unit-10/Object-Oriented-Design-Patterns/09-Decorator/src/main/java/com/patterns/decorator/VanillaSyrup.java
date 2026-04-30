package com.patterns.decorator;

/** Decorator — adds vanilla syrup (+$0.75). */
public class VanillaSyrup extends CondimentDecorator {

    public VanillaSyrup(Beverage beverage) { super(beverage); }

    @Override
    public String getDescription() { return beverage.getDescription() + ", Vanilla Syrup"; }

    @Override
    public int getCostCents()      { return beverage.getCostCents() + 75; }
}
