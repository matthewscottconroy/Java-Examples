package com.generics.methods;

/**
 * A generic class with two independent type parameters.
 *
 * <p>K and V are both resolved at the use site — they can be the same type
 * or completely different types.  Compare to {@code Map.Entry<K, V>} in the
 * standard library, which uses the same convention.
 */
public class Pair<K, V> {

    private final K first;
    private final V second;

    public Pair(K first, V second) {
        this.first  = first;
        this.second = second;
    }

    public K getFirst()  { return first;  }
    public V getSecond() { return second; }

    /**
     * Returns a new Pair with the type parameters flipped.
     * Note that the return type is {@code Pair<V, K>} — the type params swap too.
     */
    public Pair<V, K> swap() {
        return new Pair<>(second, first);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
