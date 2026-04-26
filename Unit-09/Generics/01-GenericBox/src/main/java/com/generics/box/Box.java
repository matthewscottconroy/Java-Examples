package com.generics.box;

/**
 * A generic container: the type parameter {@code T} is a placeholder
 * that the compiler replaces with a concrete type at each use site.
 *
 * <p>Type parameter naming conventions (by convention only, not enforced):
 * <ul>
 *   <li>T  — general Type</li>
 *   <li>E  — Element (used in collections)</li>
 *   <li>K  — Key</li>
 *   <li>V  — Value</li>
 *   <li>N  — Number</li>
 *   <li>R  — Return type</li>
 * </ul>
 */
public class Box<T> {

    private T value;

    public Box(T value) {
        this.value = value;
    }

    // Returns exactly T — no cast needed at the call site.
    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Box[" + value + "]";
    }
}
