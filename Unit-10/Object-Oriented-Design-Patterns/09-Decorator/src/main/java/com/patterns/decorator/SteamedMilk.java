package com.patterns.decorator;

/** Decorator — adds steamed milk (+$0.50). */
public class SteamedMilk extends CondimentDecorator {

    public SteamedMilk(Beverage beverage) { super(beverage); }

    @Override
    public String getDescription() { return beverage.getDescription() + ", Steamed Milk"; }

    @Override
    public int getCostCents()      { return beverage.getCostCents() + 50; }
}
