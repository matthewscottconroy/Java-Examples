package com.examples.math;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Permutation")
class PermutationTest {

    @Test
    @DisplayName("identity permutation leaves a list unchanged")
    void identityLeavesListUnchanged() {
        Permutation id = Permutation.identity(4);
        List<String> items = List.of("A", "B", "C", "D");
        assertEquals(items, id.apply(items));
    }

    @Test
    @DisplayName("identity is detected correctly")
    void identityIsDetected() {
        assertTrue(Permutation.identity(3).isIdentity());
        assertFalse(new Permutation(new int[]{1, 0, 2}).isIdentity());
    }

    @Test
    @DisplayName("apply rearranges elements according to the mapping")
    void applyRearrangesCorrectly() {
        // mapping[i] = j means position i draws from index j
        // [2,0,3,1] => pos0 <- index2 (Carol), pos1 <- index0 (Alice), etc.
        Permutation p = new Permutation(new int[]{2, 0, 3, 1});
        List<String> result = p.apply(List.of("Alice", "Bob", "Carol", "Dave"));
        assertEquals(List.of("Carol", "Alice", "Dave", "Bob"), result);
    }

    @Test
    @DisplayName("apply throws when list size doesn't match permutation size")
    void applyThrowsOnSizeMismatch() {
        Permutation p = new Permutation(new int[]{1, 0});
        assertThrows(IllegalArgumentException.class, () -> p.apply(List.of("A", "B", "C")));
    }

    @Test
    @DisplayName("composing p with its inverse gives the identity")
    void composeWithInverseIsIdentity() {
        Permutation p = new Permutation(new int[]{2, 0, 3, 1});
        assertTrue(p.thenApply(p.inverse()).isIdentity());
        assertTrue(p.inverse().thenApply(p).isIdentity());
    }

    @Test
    @DisplayName("composition is associative")
    void compositionIsAssociative() {
        Permutation a = new Permutation(new int[]{1, 2, 0});
        Permutation b = new Permutation(new int[]{2, 0, 1});
        Permutation c = new Permutation(new int[]{0, 2, 1});

        Permutation ab_c = a.thenApply(b).thenApply(c);
        Permutation a_bc = a.thenApply(b.thenApply(c));
        assertEquals(ab_c, a_bc);
    }

    @Test
    @DisplayName("rotation shifts elements left")
    void rotationShiftsLeft() {
        Permutation rot1 = Permutation.rotation(4, 1);
        List<String> result = rot1.apply(List.of("A", "B", "C", "D"));
        assertEquals(List.of("B", "C", "D", "A"), result);
    }

    @Test
    @DisplayName("rotation by n is the identity")
    void rotationByNIsIdentity() {
        assertTrue(Permutation.rotation(5, 5).isIdentity());
        assertTrue(Permutation.rotation(5, 0).isIdentity());
    }

    @Test
    @DisplayName("swap exchanges exactly two positions")
    void swapExchangesTwoPositions() {
        Permutation s = Permutation.swap(4, 1, 3);
        List<String> result = s.apply(List.of("A", "B", "C", "D"));
        assertEquals(List.of("A", "D", "C", "B"), result);
    }

    @Test
    @DisplayName("a swap applied twice is the identity")
    void swapTwiceIsIdentity() {
        Permutation s = Permutation.swap(4, 0, 2);
        assertTrue(s.thenApply(s).isIdentity());
    }

    @Test
    @DisplayName("order of a swap is 2")
    void orderOfSwapIsTwo() {
        assertEquals(2, Permutation.swap(4, 0, 1).order());
    }

    @Test
    @DisplayName("order of a 3-cycle is 3")
    void orderOfThreeCycleIsThree() {
        // (0 1 2): 0->1->2->0
        Permutation threeCycle = new Permutation(new int[]{1, 2, 0});
        assertEquals(3, threeCycle.order());
    }

    @Test
    @DisplayName("order of the identity is 1")
    void orderOfIdentityIsOne() {
        assertEquals(1, Permutation.identity(4).order());
    }

    @Test
    @DisplayName("cycle notation for the identity contains only fixed-point cycles")
    void cycleNotationForIdentity() {
        String notation = Permutation.identity(3).toCycleNotation();
        assertEquals("(0)(1)(2)", notation);
    }

    @Test
    @DisplayName("cycle notation for a 3-cycle is correct")
    void cycleNotationForThreeCycle() {
        Permutation p = new Permutation(new int[]{1, 2, 0});
        assertEquals("(0 1 2)", p.toCycleNotation());
    }

    @Test
    @DisplayName("constructor rejects duplicate values")
    void constructorRejectsDuplicates() {
        assertThrows(IllegalArgumentException.class, () -> new Permutation(new int[]{0, 0, 2}));
    }

    @Test
    @DisplayName("constructor rejects out-of-range values")
    void constructorRejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new Permutation(new int[]{0, 1, 5}));
    }

    @Test
    @DisplayName("equals and hashCode are consistent")
    void equalsAndHashCode() {
        Permutation p1 = new Permutation(new int[]{1, 0, 2});
        Permutation p2 = new Permutation(new int[]{1, 0, 2});
        Permutation p3 = new Permutation(new int[]{0, 1, 2});

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    @DisplayName("factorial is computed correctly")
    void factorialValues() {
        assertEquals(1, Permutation.factorial(0));
        assertEquals(1, Permutation.factorial(1));
        assertEquals(6, Permutation.factorial(3));
        assertEquals(24, Permutation.factorial(4));
        assertEquals(120, Permutation.factorial(5));
    }
}
