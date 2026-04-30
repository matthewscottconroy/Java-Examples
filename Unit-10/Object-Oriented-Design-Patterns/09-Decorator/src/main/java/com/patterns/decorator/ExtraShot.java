package com.patterns.decorator;

/** Decorator — adds an extra espresso shot (+$0.80). */
public class ExtraShot extends CondimentDecorator {

    public ExtraShot(Beverage beverage) { super(beverage); }

    @Override
    public String getDescription() { return beverage.getDescription() + ", Extra Shot"; }

    @Override
    public int getCostCents()      { return beverage.getCostCents() + 80; }
}
