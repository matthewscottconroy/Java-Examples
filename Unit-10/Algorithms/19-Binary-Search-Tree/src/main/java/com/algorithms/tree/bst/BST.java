package com.algorithms.tree.bst;

import java.util.*;

/**
 * Generic Binary Search Tree with in-order traversal, insertion, search,
 * deletion, min/max, floor/ceiling, and height.
 *
 * <p>BST invariant: for every node N,
 * all keys in N's left subtree are < N.key, and
 * all keys in N's right subtree are > N.key.
 *
 * <p>Average case: O(log n) for insert, search, delete.
 * Worst case (sorted insertion): O(n) — degenerates to a linked list.
 * Use a self-balancing tree (AVL, Red-Black) to guarantee O(log n).
 *
 * @param <K> key type (must be Comparable)
 * @param <V> value type
 */
public class BST<K extends Comparable<K>, V> {

    private record Node<K, V>(K key, V value, Node<K, V> left, Node<K, V> right) {}

    private Node<K, V> root;
    private int size;

    /** Inserts or updates the key-value pair. */
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node<K, V> put(Node<K, V> node, K key, V value) {
        if (node == null) { size++; return new Node<>(key, value, null, null); }
        int c = key.compareTo(node.key());
        if      (c < 0) return new Node<>(node.key(), node.value(), put(node.left(),  key, value), node.right());
        else if (c > 0) return new Node<>(node.key(), node.value(), node.left(),  put(node.right(), key, value));
        else            return new Node<>(key, value, node.left(), node.right());  // update
    }

    /** Returns the value for the key, or null if absent. */
    public V get(K key) {
        Node<K, V> node = root;
        while (node != null) {
            int c = key.compareTo(node.key());
            if      (c < 0) node = node.left();
            else if (c > 0) node = node.right();
            else            return node.value();
        }
        return null;
    }

    public boolean contains(K key) { return get(key) != null; }

    /** Returns all keys in sorted (in-order) sequence. */
    public List<K> inOrder() {
        List<K> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }

    private void inOrder(Node<K, V> node, List<K> out) {
        if (node == null) return;
        inOrder(node.left(), out);
        out.add(node.key());
        inOrder(node.right(), out);
    }

    /** Returns the minimum key. */
    public Optional<K> min() {
        if (root == null) return Optional.empty();
        Node<K, V> node = root;
        while (node.left() != null) node = node.left();
        return Optional.of(node.key());
    }

    /** Returns the maximum key. */
    public Optional<K> max() {
        if (root == null) return Optional.empty();
        Node<K, V> node = root;
        while (node.right() != null) node = node.right();
        return Optional.of(node.key());
    }

    /** Returns the largest key ≤ target (floor), or empty if none exists. */
    public Optional<K> floor(K key) {
        Node<K, V> result = floor(root, key);
        return result == null ? Optional.empty() : Optional.of(result.key());
    }

    private Node<K, V> floor(Node<K, V> node, K key) {
        if (node == null) return null;
        int c = key.compareTo(node.key());
        if (c == 0) return node;
        if (c <  0) return floor(node.left(), key);
        Node<K, V> t = floor(node.right(), key);
        return t != null ? t : node;
    }

    /** Returns the smallest key ≥ target (ceiling), or empty if none exists. */
    public Optional<K> ceiling(K key) {
        Node<K, V> result = ceiling(root, key);
        return result == null ? Optional.empty() : Optional.of(result.key());
    }

    private Node<K, V> ceiling(Node<K, V> node, K key) {
        if (node == null) return null;
        int c = key.compareTo(node.key());
        if (c == 0) return node;
        if (c >  0) return ceiling(node.right(), key);
        Node<K, V> t = ceiling(node.left(), key);
        return t != null ? t : node;
    }

    /** Returns the height of the tree (0 for empty, 1 for single node). */
    public int height() { return height(root); }

    private int height(Node<K, V> node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.left()), height(node.right()));
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
}
