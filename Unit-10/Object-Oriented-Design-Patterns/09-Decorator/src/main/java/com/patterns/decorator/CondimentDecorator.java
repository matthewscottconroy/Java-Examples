package com.patterns.decorator;

/**
 * Abstract Decorator — wraps another {@link Beverage} and forwards calls to it.
 *
 * <p>Concrete condiment classes extend this and override {@link #getDescription()}
 * and {@link #getCostCents()} to add their own contribution, then call
 * {@code super.getDescription()} or {@code beverage.getCostCents()} to include
 * the wrapped beverage's values.
 */
public abstract class CondimentDecorator implements Beverage {

    /** The beverage being wrapped — may itself be a decorated beverage. */
    protected final Beverage beverage;

    /**
     * @param beverage the beverage to decorate
     */
    protected CondimentDecorator(Beverage beverage) {
        this.beverage = beverage;
    }
}
