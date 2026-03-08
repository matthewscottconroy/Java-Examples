package com.examples.math;

import java.util.function.Function;

/**
 * A structure-preserving map (isomorphism) between two arrangements.
 *
 * <p>A {@code StructureMap<T, U>} is a bijection φ between element sets that
 * demonstrates the fundamental isomorphism of symmetric groups: it doesn't
 * matter <em>what</em> you're shuffling, only <em>how many</em> things you
 * have. Rearranging performers is structurally identical to rearranging numbers.
 *
 * <h2>The isomorphism property</h2>
 * <p>φ commutes with every permutation σ:
 * <pre>
 *   φ(σ(arrangement_A))  ==  σ(φ(arrangement_A))
 *                ↑                   ↑
 *     "permute first, then relabel"  "relabel first, then permute"
 * </pre>
 *
 * <p>Because permutations act on <em>positions</em> while φ acts on
 * <em>element values</em>, these two operations never interfere — so the
 * property holds for <em>any</em> bijection φ. This is the deep reason why
 * S_n is an abstract group: it depends only on n, never on the elements.
 *
 * <h2>Example</h2>
 * <pre>
 *   Arrangement&lt;String&gt; performers = Arrangement.of("Alice", "Bob", "Carol");
 *   StructureMap&lt;String, Integer&gt; map = new StructureMap&lt;&gt;(performers,
 *       name -> switch (name) {
 *           case "Alice" -> 1;
 *           case "Bob"   -> 2;
 *           default      -> 3;
 *       });
 *   map.applyToSource();   // Arrangement.of(1, 2, 3)
 *   map.preservesStructure(Permutation.swap(3, 0, 1));  // true (always)
 * </pre>
 */
public final class StructureMap<T, U> {

    private final Arrangement<T> source;
    private final Function<T, U> elementMap;

    /**
     * Creates a structure map from a source arrangement and an element-level bijection.
     *
     * @param source     the base arrangement being mapped
     * @param elementMap a bijection from T to U applied element-by-element
     */
    public StructureMap(Arrangement<T> source, Function<T, U> elementMap) {
        this.source = source;
        this.elementMap = elementMap;
    }

    /** Applies the element bijection to the source arrangement (no permutation). */
    public Arrangement<U> applyToSource() {
        return source.map(elementMap);
    }

    /**
     * Applies the element bijection to any arrangement with compatible elements.
     * The order is preserved; only labels change.
     */
    public Arrangement<U> apply(Arrangement<T> arrangement) {
        return arrangement.map(elementMap);
    }

    /**
     * Verifies the isomorphism property for permutation {@code p}:
     *
     * <pre>
     *   φ(p(source))  ==  p(φ(source))
     * </pre>
     *
     * <p>Left side:  permute the source, then relabel.<br>
     * Right side: relabel the source, then permute.<br>
     * They should always be equal — this method lets you see that directly.
     *
     * @return {@code true} if the property holds (it always should for any bijection φ)
     */
    public boolean preservesStructure(Permutation p) {
        Arrangement<U> permuteFirstThenLabel = source.permute(p).map(elementMap);
        Arrangement<U> labelFirstThenPermute = applyToSource().permute(p);
        return permuteFirstThenLabel.equals(labelFirstThenPermute);
    }

    /**
     * Checks the isomorphism property for all swaps and rotations of this size.
     * Since swaps generate the full symmetric group, this is a complete verification.
     *
     * @return {@code true} if the map is a valid isomorphism (always true for bijections)
     */
    public boolean isValidIsomorphism() {
        int n = source.size();
        // Check all transpositions (i, j): they generate S_n
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!preservesStructure(Permutation.swap(n, i, j))) return false;
            }
        }
        // Check all rotations for good measure
        for (int k = 0; k < n; k++) {
            if (!preservesStructure(Permutation.rotation(n, k))) return false;
        }
        return true;
    }

    /** Returns the source arrangement this map is defined over. */
    public Arrangement<T> getSource() {
        return source;
    }

    /** Returns the element-level bijection. */
    public Function<T, U> getElementMap() {
        return elementMap;
    }

    /**
     * Convenience factory: creates a structure map that replaces each element
     * with its zero-based position index in the source arrangement.
     *
     * <p>This is the canonical isomorphism between any arrangement and the
     * "pure numbers" arrangement [0, 1, 2, …, n−1].
     */
    public static <T> StructureMap<T, Integer> toIndices(Arrangement<T> source) {
        return new StructureMap<>(source, element -> {
            for (int i = 0; i < source.size(); i++) {
                if (source.get(i).equals(element)) return i;
            }
            throw new IllegalArgumentException("Element not in source arrangement: " + element);
        });
    }
}
