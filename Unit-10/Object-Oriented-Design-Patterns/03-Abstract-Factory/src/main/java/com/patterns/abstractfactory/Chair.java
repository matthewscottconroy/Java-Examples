package com.patterns.abstractfactory;

/**
 * Abstract Product — a chair of any style.
 *
 * <p>Client code works only with this interface; it never knows whether
 * it received a birch-wood Scandinavian chair or a welded-steel Industrial one.
 */
public interface Chair {
    /** @return the chair's material (e.g., "birch wood", "brushed steel") */
    String getMaterial();
    /** @return a one-line description of the chair */
    String describe();
}
