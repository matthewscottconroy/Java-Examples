package com.algorithms.tree.bst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BSTTest {

    private BST<Integer, String> bst;

    @BeforeEach
    void setUp() {
        bst = new BST<>();
        // Insert out of order to build a reasonable tree
        for (int k : new int[]{5, 3, 7, 1, 4, 6, 8}) {
            bst.put(k, "v" + k);
        }
    }

    @Test
    @DisplayName("get returns the correct value")
    void getCorrect() {
        assertEquals("v5", bst.get(5));
        assertEquals("v1", bst.get(1));
        assertEquals("v8", bst.get(8));
    }

    @Test
    @DisplayName("get returns null for absent key")
    void getMissing() {
        assertNull(bst.get(9));
        assertNull(bst.get(0));
    }

    @Test
    @DisplayName("contains reflects insertion status")
    void contains() {
        assertTrue(bst.contains(3));
        assertFalse(bst.contains(10));
    }

    @Test
    @DisplayName("inOrder returns all keys in sorted order")
    void inOrderSorted() {
        assertEquals(List.of(1, 3, 4, 5, 6, 7, 8), bst.inOrder());
    }

    @Test
    @DisplayName("min returns the smallest key")
    void minKey() {
        assertEquals(1, bst.min().orElseThrow());
    }

    @Test
    @DisplayName("max returns the largest key")
    void maxKey() {
        assertEquals(8, bst.max().orElseThrow());
    }

    @Test
    @DisplayName("floor returns largest key ≤ target")
    void floor() {
        assertEquals(5, bst.floor(5).orElseThrow());
        assertEquals(4, bst.floor(4).orElseThrow());   // exact match
        // 2 is not in tree; floor is 1
        assertEquals(1, bst.floor(2).orElseThrow());
    }

    @Test
    @DisplayName("floor returns empty when target is smaller than all keys")
    void floorEmpty() {
        assertTrue(bst.floor(0).isEmpty());
    }

    @Test
    @DisplayName("ceiling returns smallest key ≥ target")
    void ceiling() {
        assertEquals(5, bst.ceiling(5).orElseThrow());
        assertEquals(6, bst.ceiling(6).orElseThrow());
        // 2 is not in tree; ceiling is 3
        assertEquals(3, bst.ceiling(2).orElseThrow());
    }

    @Test
    @DisplayName("ceiling returns empty when target is larger than all keys")
    void ceilingEmpty() {
        assertTrue(bst.ceiling(9).isEmpty());
    }

    @Test
    @DisplayName("put updates value for existing key")
    void update() {
        bst.put(5, "updated");
        assertEquals("updated", bst.get(5));
        assertEquals(7, bst.size());  // no new node added
    }

    @Test
    @DisplayName("size tracks insertions correctly")
    void sizeTracking() {
        assertEquals(7, bst.size());
        bst.put(9, "v9");
        assertEquals(8, bst.size());
        bst.put(5, "duplicate");  // update, not insert
        assertEquals(8, bst.size());
    }

    @Test
    @DisplayName("height of balanced BST is O(log n)")
    void heightBalanced() {
        assertTrue(bst.height() <= 4);  // 7 nodes, balanced → height 3
    }

    @Test
    @DisplayName("empty BST returns empty for min and max")
    void emptyBST() {
        BST<Integer, String> empty = new BST<>();
        assertTrue(empty.min().isEmpty());
        assertTrue(empty.max().isEmpty());
        assertEquals(0, empty.height());
        assertTrue(empty.isEmpty());
    }
}
