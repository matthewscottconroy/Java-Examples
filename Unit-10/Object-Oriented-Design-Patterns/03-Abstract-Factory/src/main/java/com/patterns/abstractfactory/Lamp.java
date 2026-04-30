package com.patterns.abstractfactory;

/**
 * Abstract Product — a floor lamp of any style.
 */
public interface Lamp {
    /** @return the lamp's material */
    String getMaterial();
    /** @return a one-line description of the lamp */
    String describe();
}
