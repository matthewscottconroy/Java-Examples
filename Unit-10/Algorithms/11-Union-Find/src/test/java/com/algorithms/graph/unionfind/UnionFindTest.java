package com.algorithms.graph.unionfind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnionFindTest {

    private UnionFind uf;

    @BeforeEach
    void setUp() {
        uf = new UnionFind(10);
    }

    @Test
    @DisplayName("initially all elements are in separate components")
    void initialState() {
        assertEquals(10, uf.componentCount());
        for (int i = 0; i < 10; i++) assertFalse(uf.connected(i, (i + 1) % 10));
    }

    @Test
    @DisplayName("union merges two components")
    void unionMerges() {
        assertTrue(uf.union(0, 1));
        assertEquals(9, uf.componentCount());
        assertTrue(uf.connected(0, 1));
    }

    @Test
    @DisplayName("union of already-connected elements returns false")
    void unionSameComponent() {
        uf.union(0, 1);
        assertFalse(uf.union(0, 1));
        assertFalse(uf.union(1, 0));
        assertEquals(9, uf.componentCount());
    }

    @Test
    @DisplayName("connectivity is transitive")
    void transitiveConnectivity() {
        uf.union(0, 1);
        uf.union(1, 2);
        uf.union(2, 3);
        assertTrue(uf.connected(0, 3));
    }

    @Test
    @DisplayName("component count decreases correctly as unions are added")
    void componentCount() {
        assertEquals(10, uf.componentCount());
        uf.union(0, 1);  // 9
        uf.union(2, 3);  // 8
        uf.union(0, 2);  // 7 (merges {0,1} and {2,3})
        assertEquals(7, uf.componentCount());
    }

    @Test
    @DisplayName("find is idempotent — calling twice gives same result")
    void findIdempotent() {
        uf.union(3, 7);
        assertEquals(uf.find(3), uf.find(7));
        assertEquals(uf.find(3), uf.find(3));
    }

    @Test
    @DisplayName("non-connected elements remain separate")
    void nonConnected() {
        uf.union(0, 1);
        uf.union(2, 3);
        assertFalse(uf.connected(0, 2));
        assertFalse(uf.connected(1, 3));
    }

    @Test
    @DisplayName("merging all elements yields one component")
    void mergeAll() {
        for (int i = 0; i < 9; i++) uf.union(i, i + 1);
        assertEquals(1, uf.componentCount());
        assertTrue(uf.connected(0, 9));
    }

    @Test
    @DisplayName("single element is connected to itself")
    void selfConnected() {
        assertTrue(uf.connected(5, 5));
    }
}
