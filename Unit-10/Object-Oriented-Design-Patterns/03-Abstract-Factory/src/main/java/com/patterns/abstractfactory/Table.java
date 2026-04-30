package com.patterns.abstractfactory;

/**
 * Abstract Product — a dining table of any style.
 */
public interface Table {
    /** @return the table's material */
    String getMaterial();
    /** @return a one-line description of the table */
    String describe();
}
