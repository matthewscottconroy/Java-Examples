package com.examples.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StructureMap")
class StructureMapTest {

    private static final Arrangement<String> PERFORMERS =
            Arrangement.of("Alice", "Bob", "Carol", "Dave");

    private static final Map<String, Integer> SEAT = Map.of(
            "Alice", 1, "Bob", 2, "Carol", 3, "Dave", 4);

    private StructureMap<String, Integer> isoMap() {
        return new StructureMap<>(PERFORMERS, SEAT::get);
    }

    @Test
    @DisplayName("applyToSource relabels without permuting")
    void applyToSourceRelabels() {
        Arrangement<Integer> numbered = isoMap().applyToSource();
        assertEquals(Arrangement.of(1, 2, 3, 4), numbered);
    }

    @Test
    @DisplayName("apply relabels any compatible arrangement")
    void applyRelabelsArrangement() {
        StructureMap<String, Integer> map = isoMap();
        Arrangement<String> shuffled = Arrangement.of("Bob", "Alice", "Dave", "Carol");
        Arrangement<Integer> result  = map.apply(shuffled);
        assertEquals(Arrangement.of(2, 1, 4, 3), result);
    }

    @Test
    @DisplayName("preservesStructure holds for a swap")
    void preservesStructureForSwap() {
        assertTrue(isoMap().preservesStructure(Permutation.swap(4, 0, 1)));
    }

    @Test
    @DisplayName("preservesStructure holds for a rotation")
    void preservesStructureForRotation() {
        assertTrue(isoMap().preservesStructure(Permutation.rotation(4, 2)));
    }

    @Test
    @DisplayName("preservesStructure holds for the identity")
    void preservesStructureForIdentity() {
        assertTrue(isoMap().preservesStructure(Permutation.identity(4)));
    }

    @Test
    @DisplayName("preservesStructure holds for an arbitrary permutation")
    void preservesStructureForArbitraryPermutation() {
        Permutation p = new Permutation(new int[]{3, 1, 0, 2});
        assertTrue(isoMap().preservesStructure(p));
    }

    @Test
    @DisplayName("isValidIsomorphism passes for a valid element bijection")
    void isValidIsomorphismPasses() {
        assertTrue(isoMap().isValidIsomorphism());
    }

    @Test
    @DisplayName("toIndices factory creates the canonical index map")
    void toIndicesFactory() {
        StructureMap<String, Integer> map = StructureMap.toIndices(PERFORMERS);
        Arrangement<Integer> result = map.applyToSource();
        assertEquals(Arrangement.of(0, 1, 2, 3), result);
    }

    @Test
    @DisplayName("toIndices isomorphism is valid")
    void toIndicesIsValidIsomorphism() {
        assertTrue(StructureMap.toIndices(PERFORMERS).isValidIsomorphism());
    }

    @Test
    @DisplayName("isomorphism property holds: permute-then-label == label-then-permute")
    void isomorphismPropertyHoldsExplicitly() {
        StructureMap<String, Integer> map = isoMap();
        Permutation p = new Permutation(new int[]{2, 0, 3, 1});

        // Left side: permute source, then label
        Arrangement<Integer> left = map.apply(PERFORMERS.permute(p));

        // Right side: label source, then permute
        Arrangement<Integer> right = map.applyToSource().permute(p);

        assertEquals(left, right);
    }

    @Test
    @DisplayName("getSource returns the original arrangement")
    void getSourceReturnsOriginal() {
        assertEquals(PERFORMERS, isoMap().getSource());
    }

    @Test
    @DisplayName("structure map works with non-string element types")
    void structureMapWithIntegers() {
        Arrangement<Integer> numbers = Arrangement.of(10, 20, 30);
        StructureMap<Integer, String> map = new StructureMap<>(numbers,
                n -> switch (n) {
                    case 10 -> "ten";
                    case 20 -> "twenty";
                    default -> "thirty";
                });

        Arrangement<String> words = map.applyToSource();
        assertEquals(Arrangement.of("ten", "twenty", "thirty"), words);
        assertTrue(map.isValidIsomorphism());
    }
}
