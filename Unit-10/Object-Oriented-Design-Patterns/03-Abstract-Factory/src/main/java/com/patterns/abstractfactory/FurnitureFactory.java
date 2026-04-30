package com.patterns.abstractfactory;

/**
 * Abstract Factory — declares the creation methods for each furniture type.
 *
 * <p>Each concrete factory produces a complete, stylistically coherent
 * set of furniture. You cannot mix a Scandinavian chair with an Industrial
 * table by accident: a single factory reference guarantees consistency.
 *
 * <p><b>Pattern roles:</b>
 * <pre>
 *   FurnitureFactory          — Abstract Factory
 *   ScandinavianFactory       — Concrete Factory A
 *   IndustrialFactory         — Concrete Factory B
 *   Chair, Table, Lamp        — Abstract Products
 *   ScandinavianChair, …      — Concrete Products
 * </pre>
 */
public interface FurnitureFactory {

    /**
     * Creates a chair in this factory's style.
     * @return a style-appropriate chair
     */
    Chair createChair();

    /**
     * Creates a dining table in this factory's style.
     * @return a style-appropriate table
     */
    Table createTable();

    /**
     * Creates a floor lamp in this factory's style.
     * @return a style-appropriate lamp
     */
    Lamp createLamp();
}
