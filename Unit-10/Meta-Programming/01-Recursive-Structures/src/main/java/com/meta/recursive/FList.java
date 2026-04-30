package com.meta.recursive;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Immutable, persistent singly-linked list defined recursively.
 *
 * <p>A list is either:
 * <ul>
 *   <li>{@link Nil} — the empty list</li>
 *   <li>{@link Cons} — a head element prepended to a tail list</li>
 * </ul>
 *
 * <p>This mirrors exactly the mathematical inductive definition:
 * <pre>
 *   List[A] = Nil | Cons(head: A, tail: List[A])
 * </pre>
 *
 * <p>Every operation on a list follows the same recursive pattern:
 * base case on Nil, recursive case on Cons.
 *
 * <p>All operations are purely functional — no mutation. Sharing the
 * tail makes prepend O(1): the new Cons reuses the existing tail.
 */
public sealed interface FList<A> permits FList.Nil, FList.Cons {

    record Nil<A>() implements FList<A> {}
    record Cons<A>(A head, FList<A> tail) implements FList<A> {}

    // -----------------------------------------------------------------
    // Smart constructors
    // -----------------------------------------------------------------

    static <A> FList<A> nil() { return new Nil<>(); }

    @SafeVarargs
    static <A> FList<A> of(A... elements) {
        FList<A> result = nil();
        for (int i = elements.length - 1; i >= 0; i--) {
            result = new Cons<>(elements[i], result);
        }
        return result;
    }

    // -----------------------------------------------------------------
    // Core structural operations (all recursive over the list shape)
    // -----------------------------------------------------------------

    default boolean isEmpty() { return this instanceof Nil; }

    default A head() {
        if (this instanceof Cons<A> c) return c.head();
        throw new UnsupportedOperationException("head of empty list");
    }

    default FList<A> tail() {
        if (this instanceof Cons<A> c) return c.tail();
        throw new UnsupportedOperationException("tail of empty list");
    }

    default FList<A> prepend(A element) { return new Cons<>(element, this); }

    default int size() {
        return switch (this) {
            case Nil<A>   ignored -> 0;
            case Cons<A>  c      -> 1 + c.tail().size();
        };
    }

    // -----------------------------------------------------------------
    // Higher-order operations (structural recursion with a function arg)
    // -----------------------------------------------------------------

    /** Applies f to every element, producing a new list of the same shape. */
    default <B> FList<B> map(Function<A, B> f) {
        return switch (this) {
            case Nil<A>  ignored -> nil();
            case Cons<A> c      -> new Cons<>(f.apply(c.head()), c.tail().map(f));
        };
    }

    /** Keeps only elements satisfying the predicate. */
    default FList<A> filter(Predicate<A> p) {
        return switch (this) {
            case Nil<A>  ignored -> nil();
            case Cons<A> c      -> p.test(c.head())
                ? new Cons<>(c.head(), c.tail().filter(p))
                : c.tail().filter(p);
        };
    }

    /**
     * Reduces the list to a single value by applying f to each element
     * and an accumulator, starting from init.
     *
     * <p>fold(f, 0, [1,2,3]) = f(1, f(2, f(3, 0))) = 1+2+3 = 6
     */
    default <B> B foldRight(BiFunction<A, B, B> f, B init) {
        return switch (this) {
            case Nil<A>  ignored -> init;
            case Cons<A> c      -> f.apply(c.head(), c.tail().foldRight(f, init));
        };
    }

    /** Left fold — tail-recursive-friendly, reverses the association order. */
    default <B> B foldLeft(BiFunction<B, A, B> f, B init) {
        B acc = init;
        FList<A> current = this;
        while (current instanceof Cons<A> c) {
            acc = f.apply(acc, c.head());
            current = c.tail();
        }
        return acc;
    }

    /** Concatenates this list with other. O(n) — rebuilds the prefix. */
    default FList<A> append(FList<A> other) {
        return switch (this) {
            case Nil<A>  ignored -> other;
            case Cons<A> c      -> new Cons<>(c.head(), c.tail().append(other));
        };
    }

    /** Returns a new list with elements in reverse order. */
    default FList<A> reverse() {
        return foldLeft((acc, x) -> new Cons<>(x, acc), nil());
    }

    /** Applies f to each element and flattens the resulting lists. */
    default <B> FList<B> flatMap(Function<A, FList<B>> f) {
        return switch (this) {
            case Nil<A>  ignored -> nil();
            case Cons<A> c      -> f.apply(c.head()).append(c.tail().flatMap(f));
        };
    }

    default boolean contains(A element) {
        return switch (this) {
            case Nil<A>  ignored -> false;
            case Cons<A> c      -> c.head().equals(element) || c.tail().contains(element);
        };
    }

    default String show() {
        StringBuilder sb = new StringBuilder("[");
        FList<A> current = this;
        boolean first = true;
        while (current instanceof Cons<A> c) {
            if (!first) sb.append(", ");
            sb.append(c.head());
            current = c.tail();
            first = false;
        }
        return sb.append("]").toString();
    }
}
