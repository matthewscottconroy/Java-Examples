package com.meta.recursive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveStructuresTest {

    // ---------------------------------------------------------------
    // FList
    // ---------------------------------------------------------------

    @Test @DisplayName("FList.of() creates list in declared order")
    void flist_of() {
        FList<Integer> list = FList.of(1, 2, 3);
        assertEquals(1, list.head());
        assertEquals(2, list.tail().head());
        assertEquals(3, list.tail().tail().head());
    }

    @Test @DisplayName("FList: empty list has size 0")
    void flist_emptySize() {
        assertEquals(0, FList.<Integer>nil().size());
    }

    @Test @DisplayName("FList: size counts all elements")
    void flist_size() {
        assertEquals(5, FList.of(1, 2, 3, 4, 5).size());
    }

    @Test @DisplayName("FList: map transforms every element")
    void flist_map() {
        FList<Integer> result = FList.of(1, 2, 3).map(x -> x * 2);
        assertEquals(2, result.head());
        assertEquals(4, result.tail().head());
        assertEquals(6, result.tail().tail().head());
    }

    @Test @DisplayName("FList: map preserves size")
    void flist_map_size() {
        assertEquals(4, FList.of(10, 20, 30, 40).map(x -> x + 1).size());
    }

    @Test @DisplayName("FList: filter removes non-matching elements")
    void flist_filter() {
        FList<Integer> evens = FList.of(1, 2, 3, 4, 5).filter(x -> x % 2 == 0);
        assertEquals(2, evens.size());
        assertTrue(evens.contains(2));
        assertTrue(evens.contains(4));
    }

    @Test @DisplayName("FList: foldRight sums all elements")
    void flist_foldRight_sum() {
        int sum = FList.of(1, 2, 3, 4).foldRight(Integer::sum, 0);
        assertEquals(10, sum);
    }

    @Test @DisplayName("FList: foldLeft builds reversed string")
    void flist_foldLeft_string() {
        String result = FList.of("a", "b", "c").foldLeft((acc, x) -> acc + x, "");
        assertEquals("abc", result);
    }

    @Test @DisplayName("FList: reverse produces mirror order")
    void flist_reverse() {
        FList<Integer> rev = FList.of(1, 2, 3).reverse();
        assertEquals(3, rev.head());
        assertEquals(2, rev.tail().head());
        assertEquals(1, rev.tail().tail().head());
    }

    @Test @DisplayName("FList: prepend is O(1) — original unchanged")
    void flist_prepend_sharing() {
        FList<Integer> original = FList.of(2, 3);
        FList<Integer> extended = original.prepend(1);
        assertEquals(1, extended.head());
        // Original still starts with 2
        assertEquals(2, original.head());
    }

    @Test @DisplayName("FList: append produces concatenation")
    void flist_append() {
        FList<Integer> a = FList.of(1, 2);
        FList<Integer> b = FList.of(3, 4);
        FList<Integer> c = a.append(b);
        assertEquals(4, c.size());
        assertEquals(1, c.head());
        assertEquals(4, c.reverse().head());  // last element
    }

    @Test @DisplayName("FList: flatMap expands and flattens")
    void flist_flatMap() {
        FList<Integer> result = FList.of(1, 2, 3).flatMap(x -> FList.of(x, -x));
        assertEquals(6, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(-1));
    }

    @Test @DisplayName("FList: contains finds present element")
    void flist_contains_true() {
        assertTrue(FList.of(10, 20, 30).contains(20));
    }

    @Test @DisplayName("FList: contains returns false for absent element")
    void flist_contains_false() {
        assertFalse(FList.of(10, 20, 30).contains(99));
    }

    // ---------------------------------------------------------------
    // BTree
    // ---------------------------------------------------------------

    @Test @DisplayName("BTree: empty tree has size 0 and height 0")
    void btree_empty() {
        BTree<Integer> t = BTree.empty();
        assertEquals(0, t.size());
        assertEquals(0, t.height());
    }

    @Test @DisplayName("BTree: insert maintains BST property (in-order sorted)")
    void btree_insert_sorted() {
        BTree<Integer> t = BTree.of(5, 3, 7, 1, 4, 6, 9);
        FList<Integer> sorted = t.inOrder();
        // Verify strictly ascending by folding
        int prev = sorted.head();
        FList<Integer> rest = sorted.tail();
        while (rest instanceof FList.Cons<Integer> c) {
            assertTrue(c.head() > prev, "In-order should be ascending");
            prev = c.head();
            rest = c.tail();
        }
    }

    @Test @DisplayName("BTree: contains finds inserted element")
    void btree_contains_true() {
        assertTrue(BTree.of(5, 3, 7).contains(3));
    }

    @Test @DisplayName("BTree: contains returns false for absent element")
    void btree_contains_false() {
        assertFalse(BTree.of(5, 3, 7).contains(99));
    }

    @Test @DisplayName("BTree: duplicate insert does not change size")
    void btree_duplicate() {
        BTree<Integer> t = BTree.of(5, 3, 7);
        assertEquals(t.size(), t.insert(5).size());
    }

    @Test @DisplayName("BTree: min and max")
    void btree_minMax() {
        BTree<Integer> t = BTree.of(5, 3, 7, 1, 9);
        assertEquals(1, (int) t.min());
        assertEquals(9, (int) t.max());
    }

    @Test @DisplayName("BTree: fold sums all values")
    void btree_fold_sum() {
        BTree<Integer> t = BTree.of(1, 2, 3, 4, 5);
        int sum = t.fold((v, l, r) -> v + l + r, 0);
        assertEquals(15, sum);
    }

    @Test @DisplayName("BTree: map transforms all nodes")
    void btree_map() {
        BTree<Integer> t = BTree.of(1, 2, 3);
        BTree<Integer> doubled = t.map(x -> x * 2);
        assertTrue(doubled.contains(2));
        assertTrue(doubled.contains(4));
        assertTrue(doubled.contains(6));
    }

    @Test @DisplayName("BTree: height of balanced tree ≈ log₂(size)")
    void btree_height() {
        BTree<Integer> t = BTree.of(4, 2, 6, 1, 3, 5, 7);  // perfectly balanced
        assertEquals(7, t.size());
        assertEquals(3, t.height());  // log₂(7) ≈ 2.8 → ceil = 3
    }
}
