package com.patterns.decorator;

/**
 * Concrete Component — a plain house-blend drip coffee.
 */
public class HouseBlend implements Beverage {

    @Override
    public String getDescription() { return "House Blend Coffee"; }

    @Override
    public int getCostCents()      { return 150; } // $1.50
}
