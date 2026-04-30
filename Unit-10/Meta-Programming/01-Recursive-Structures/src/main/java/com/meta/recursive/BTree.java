package com.meta.recursive;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Immutable binary search tree defined recursively.
 *
 * <p>A tree is either:
 * <ul>
 *   <li>{@link Leaf} — empty</li>
 *   <li>{@link Branch} — a value with a left subtree and a right subtree</li>
 * </ul>
 *
 * <p>Every algorithm mirrors the tree's own shape: base case on Leaf,
 * recursive case on Branch. The resulting code reads like a proof by
 * structural induction.
 */
public sealed interface BTree<A extends Comparable<A>>
        permits BTree.Leaf, BTree.Branch {

    record Leaf<A extends Comparable<A>>() implements BTree<A> {}

    record Branch<A extends Comparable<A>>(A value, BTree<A> left, BTree<A> right)
            implements BTree<A> {}

    // -----------------------------------------------------------------
    // Smart constructors
    // -----------------------------------------------------------------

    static <A extends Comparable<A>> BTree<A> empty() { return new Leaf<>(); }

    @SafeVarargs
    static <A extends Comparable<A>> BTree<A> of(A... values) {
        BTree<A> t = empty();
        for (A v : values) t = t.insert(v);
        return t;
    }

    // -----------------------------------------------------------------
    // Core BST operations (all purely functional — return new trees)
    // -----------------------------------------------------------------

    default boolean isEmpty() { return this instanceof Leaf; }

    default int size() {
        return switch (this) {
            case Leaf<A>   ignored -> 0;
            case Branch<A> b      -> 1 + b.left().size() + b.right().size();
        };
    }

    default int height() {
        return switch (this) {
            case Leaf<A>   ignored -> 0;
            case Branch<A> b      -> 1 + Math.max(b.left().height(), b.right().height());
        };
    }

    default boolean contains(A value) {
        return switch (this) {
            case Leaf<A>   ignored -> false;
            case Branch<A> b -> {
                int cmp = value.compareTo(b.value());
                yield cmp < 0 ? b.left().contains(value)
                    : cmp > 0 ? b.right().contains(value)
                    : true;
            }
        };
    }

    /** Returns a new tree with value inserted (duplicate values are ignored). */
    default BTree<A> insert(A value) {
        return switch (this) {
            case Leaf<A>   ignored -> new Branch<>(value, empty(), empty());
            case Branch<A> b -> {
                int cmp = value.compareTo(b.value());
                yield cmp < 0 ? new Branch<>(b.value(), b.left().insert(value), b.right())
                    : cmp > 0 ? new Branch<>(b.value(), b.left(), b.right().insert(value))
                    : this;   // already present
            }
        };
    }

    /** In-order traversal produces a sorted list. */
    default FList<A> inOrder() {
        return switch (this) {
            case Leaf<A>   ignored -> FList.nil();
            case Branch<A> b      ->
                b.left().inOrder().append(FList.of(b.value())).append(b.right().inOrder());
        };
    }

    /** Applies f to every node value, preserving tree shape. */
    default <B extends Comparable<B>> BTree<B> map(Function<A, B> f) {
        return switch (this) {
            case Leaf<A>   ignored -> empty();
            case Branch<A> b      ->
                new Branch<>(f.apply(b.value()), b.left().map(f), b.right().map(f));
        };
    }

    /**
     * Fold over the tree structure: combine subtree results with a function.
     *
     * <p>fold(f, z, Branch(v, l, r)) = f(v, fold(f, z, l), fold(f, z, r))
     */
    default <B> B fold(TriFunction<A, B, B, B> f, B zero) {
        return switch (this) {
            case Leaf<A>   ignored -> zero;
            case Branch<A> b      -> f.apply(b.value(), b.left().fold(f, zero),
                                                         b.right().fold(f, zero));
        };
    }

    /** Minimum value (leftmost node). */
    default A min() {
        return switch (this) {
            case Leaf<A>   ignored -> throw new UnsupportedOperationException("empty tree");
            case Branch<A> b      -> b.left().isEmpty() ? b.value() : b.left().min();
        };
    }

    /** Maximum value (rightmost node). */
    default A max() {
        return switch (this) {
            case Leaf<A>   ignored -> throw new UnsupportedOperationException("empty tree");
            case Branch<A> b      -> b.right().isEmpty() ? b.value() : b.right().max();
        };
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
