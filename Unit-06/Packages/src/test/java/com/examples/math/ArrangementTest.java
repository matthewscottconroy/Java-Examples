package com.examples.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Arrangement")
class ArrangementTest {

    @Test
    @DisplayName("of() factory creates the arrangement in the given order")
    void factoryCreatesInOrder() {
        Arrangement<String> a = Arrangement.of("Alice", "Bob", "Carol");
        assertEquals("Alice", a.get(0));
        assertEquals("Bob",   a.get(1));
        assertEquals("Carol", a.get(2));
        assertEquals(3, a.size());
    }

    @Test
    @DisplayName("permute applies a permutation correctly")
    void permuteAppliesPermutation() {
        Arrangement<String> lineup = Arrangement.of("Alice", "Bob", "Carol", "Dave");
        Permutation rotate = Permutation.rotation(4, 1);
        Arrangement<String> rotated = lineup.permute(rotate);
        assertEquals(Arrangement.of("Bob", "Carol", "Dave", "Alice"), rotated);
    }

    @Test
    @DisplayName("permuting by the identity leaves the arrangement unchanged")
    void permuteByIdentityIsUnchanged() {
        Arrangement<Integer> a = Arrangement.of(10, 20, 30);
        assertEquals(a, a.permute(Permutation.identity(3)));
    }

    @Test
    @DisplayName("countOrderings returns n!")
    void countOrderings() {
        assertEquals(1,   Arrangement.of("A").countOrderings());
        assertEquals(2,   Arrangement.of("A", "B").countOrderings());
        assertEquals(6,   Arrangement.of("A", "B", "C").countOrderings());
        assertEquals(24,  Arrangement.of("A", "B", "C", "D").countOrderings());
        assertEquals(120, Arrangement.of("A", "B", "C", "D", "E").countOrderings());
    }

    @Test
    @DisplayName("allOrderings returns exactly n! distinct arrangements")
    void allOrderingsCount() {
        Arrangement<String> a = Arrangement.of("X", "Y", "Z");
        List<Arrangement<String>> all = a.allOrderings();
        assertEquals(6, all.size());
        // All should be distinct
        assertEquals(6, all.stream().distinct().count());
    }

    @Test
    @DisplayName("allOrderings for a single-element arrangement returns one ordering")
    void allOrderingsSingleElement() {
        List<Arrangement<String>> all = Arrangement.of("Only").allOrderings();
        assertEquals(1, all.size());
        assertEquals(Arrangement.of("Only"), all.get(0));
    }

    @Test
    @DisplayName("map transforms each element by the given function")
    void mapTransformsElements() {
        Arrangement<String> names = Arrangement.of("Alice", "Bob", "Carol");
        Arrangement<Integer> lengths = names.map(String::length);
        assertEquals(Arrangement.of(5, 3, 5), lengths);
    }

    @Test
    @DisplayName("permutationTo finds the permutation between two arrangements")
    void permutationToCorrect() {
        Arrangement<String> from = Arrangement.of("Carol", "Alice", "Bob");
        Arrangement<String> to   = Arrangement.of("Alice", "Bob", "Carol");
        Permutation p = from.permutationTo(to);
        assertEquals(to, from.permute(p));
    }

    @Test
    @DisplayName("permutationTo with identity arrangements gives identity permutation")
    void permutationToSameArrangementIsIdentity() {
        Arrangement<String> a = Arrangement.of("A", "B", "C");
        Permutation p = a.permutationTo(a);
        assertTrue(p.isIdentity());
    }

    @Test
    @DisplayName("permutationTo throws when sizes differ")
    void permutationToThrowsOnDifferentSizes() {
        Arrangement<String> a = Arrangement.of("A", "B");
        Arrangement<String> b = Arrangement.of("A", "B", "C");
        assertThrows(IllegalArgumentException.class, () -> a.permutationTo(b));
    }

    @Test
    @DisplayName("permute throws when permutation size doesn't match arrangement size")
    void permuteThrowsOnSizeMismatch() {
        Arrangement<String> a = Arrangement.of("X", "Y", "Z");
        Permutation p = Permutation.identity(4);
        assertThrows(IllegalArgumentException.class, () -> a.permute(p));
    }

    @Test
    @DisplayName("toList returns an unmodifiable view")
    void toListIsUnmodifiable() {
        Arrangement<String> a = Arrangement.of("A", "B", "C");
        assertThrows(UnsupportedOperationException.class, () -> a.toList().add("D"));
    }

    @Test
    @DisplayName("equals and hashCode are value-based")
    void equalsAndHashCode() {
        Arrangement<String> a1 = Arrangement.of("X", "Y");
        Arrangement<String> a2 = Arrangement.of("X", "Y");
        Arrangement<String> a3 = Arrangement.of("Y", "X");

        assertEquals(a1, a2);
        assertNotEquals(a1, a3);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    @DisplayName("composing permute calls matches composing the permutations in reverse order")
    void composedPermuteMatchesDirectCompose() {
        // a.permute(p).permute(q)[i]
        //   = a.permute(p)[ q.map(i) ]
        //   = a[ p.map(q.map(i)) ]
        //   = a[ q.thenApply(p).map(i) ]
        // So chaining permute(p) then permute(q) equals a single permute(q.thenApply(p)).
        Arrangement<String> a = Arrangement.of("Alice", "Bob", "Carol", "Dave");
        Permutation p = Permutation.rotation(4, 1);
        Permutation q = Permutation.swap(4, 0, 2);

        Arrangement<String> sequential = a.permute(p).permute(q);
        Arrangement<String> composed   = a.permute(q.thenApply(p));
        assertEquals(sequential, composed);
    }
}
