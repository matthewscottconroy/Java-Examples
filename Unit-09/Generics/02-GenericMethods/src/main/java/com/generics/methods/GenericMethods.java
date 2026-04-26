package com.generics.methods;

import java.util.Arrays;
import java.util.List;

/**
 * A collection of static generic methods.
 *
 * <p>A generic method declares its own type parameter(s) in angle brackets
 * <em>before the return type</em>.  These are independent of any type
 * parameter on the enclosing class — even a non-generic class can host them.
 *
 * <p>Syntax reminder:
 * <pre>
 *   public static &lt;T&gt; T identity(T value) { return value; }
 *                  ^^^
 *                  type parameter declaration — must appear before the return type
 * </pre>
 */
public class GenericMethods {

    // Returns exactly what was passed in.
    // The compiler infers T from the argument: identity("hello") → T = String.
    public static <T> T identity(T value) {
        return value;
    }

    // Works on arrays of any element type.
    // E is conventional when the method is "element-centric" (collection-like).
    public static <E> void printArray(E[] array) {
        System.out.println(Arrays.toString(array));
    }

    // In-place swap of two positions in an array.
    // T is inferred from the array's declared element type.
    public static <T> void swap(T[] array, int i, int j) {
        T temp   = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    // Wraps two values into a Pair.
    // Two separate type parameters: A and B can be any combination of types.
    public static <A, B> Pair<A, B> pairOf(A a, B b) {
        return new Pair<>(a, b);
    }

    // Returns a Pair where both elements have the same type.
    // Uses a single parameter T for both slots.
    public static <T> Pair<T, T> duplicate(T value) {
        return new Pair<>(value, value);
    }

    // Preview of example 03: a bounded type parameter.
    // <T extends Comparable<T>> means T must be comparable to itself —
    // required to call compareTo().  More on bounds in 03-BoundedTypes.
    public static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    // Returns an empty, immutable list typed to T.
    // Rarely need a type witness, but the syntax is shown in Main.
    public static <T> List<T> emptyList() {
        return List.of();
    }
}
