package com.patterns.decorator;

/** Decorator — adds whipped cream (+$0.60). */
public class WhipCream extends CondimentDecorator {

    public WhipCream(Beverage beverage) { super(beverage); }

    @Override
    public String getDescription() { return beverage.getDescription() + ", Whipped Cream"; }

    @Override
    public int getCostCents()      { return beverage.getCostCents() + 60; }
}
