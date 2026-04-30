package com.generics.methods;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericMethodsTest {

    // -- identity --

    @Test @DisplayName("identity returns the same String reference")
    void identity_string() {
        String s = "hello";
        assertSame(s, GenericMethods.identity(s));
    }

    @Test @DisplayName("identity returns the same Integer reference")
    void identity_integer() {
        Integer i = 999;
        assertSame(i, GenericMethods.identity(i));
    }

    // -- swap --

    @Test @DisplayName("swap exchanges two array elements")
    void swap_basic() {
        String[] arr = {"a", "b", "c"};
        GenericMethods.swap(arr, 0, 2);
        assertArrayEquals(new String[]{"c", "b", "a"}, arr);
    }

    @Test @DisplayName("swap on same index leaves array unchanged")
    void swap_sameIndex() {
        Integer[] arr = {1, 2, 3};
        GenericMethods.swap(arr, 1, 1);
        assertArrayEquals(new Integer[]{1, 2, 3}, arr);
    }

    // -- pairOf --

    @Test @DisplayName("pairOf stores both values")
    void pairOf_values() {
        Pair<String, Integer> p = GenericMethods.pairOf("key", 42);
        assertEquals("key", p.getFirst());
        assertEquals(42,    p.getSecond());
    }

    // -- duplicate --

    @Test @DisplayName("duplicate produces a pair with identical values")
    void duplicate_sameValue() {
        Pair<String, String> p = GenericMethods.duplicate("x");
        assertEquals("x", p.getFirst());
        assertEquals("x", p.getSecond());
    }

    // -- max --

    @Test @DisplayName("max returns the larger integer")
    void max_integers() {
        assertEquals(7, GenericMethods.max(3, 7));
    }

    @Test @DisplayName("max returns the later string lexicographically")
    void max_strings() {
        assertEquals("zebra", GenericMethods.max("apple", "zebra"));
    }

    @Test @DisplayName("max of equal values returns one of them")
    void max_equal() {
        assertEquals(5, GenericMethods.max(5, 5));
    }

    // -- emptyList --

    @Test @DisplayName("emptyList returns an empty list")
    void emptyList_isEmpty() {
        List<String> list = GenericMethods.emptyList();
        assertTrue(list.isEmpty());
    }
}

class PairTest {

    @Test @DisplayName("getFirst and getSecond return constructor values")
    void accessors() {
        Pair<Integer, String> p = new Pair<>(1, "one");
        assertEquals(1,     p.getFirst());
        assertEquals("one", p.getSecond());
    }

    @Test @DisplayName("swap returns a new Pair with types flipped")
    void swap_flipsValues() {
        Pair<String, Integer> p = new Pair<>("hello", 42);
        Pair<Integer, String> swapped = p.swap();
        assertEquals(42,      swapped.getFirst());
        assertEquals("hello", swapped.getSecond());
    }

    @Test @DisplayName("toString formats as (first, second)")
    void toString_format() {
        assertEquals("(a, 1)", new Pair<>("a", 1).toString());
    }
}
