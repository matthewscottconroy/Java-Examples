package com.algorithms.graph.dfs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DFSTest {

    @Test
    @DisplayName("dfsOrder visits all reachable vertices in post-order")
    void dfsOrderBasic() {
        // 0→1, 0→2, 1→3
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        List<Integer> order = DFS.dfsOrder(g, 0);
        // 0 must appear last (post-order from source)
        assertEquals(4, order.size());
        assertEquals(0, order.get(order.size() - 1));
        // 1 must appear after 3 (3 is a leaf below 1)
        assertTrue(order.indexOf(3) < order.indexOf(1));
    }

    @Test
    @DisplayName("hasCycleDirected detects a back edge")
    void cycleDirected() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);  // cycle: 1→2→3→1
        assertTrue(DFS.hasCycleDirected(g));
    }

    @Test
    @DisplayName("hasCycleDirected returns false on a DAG")
    void noCycleDirected() {
        Graph g = new Graph(4);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        assertFalse(DFS.hasCycleDirected(g));
    }

    @Test
    @DisplayName("hasCycleDirected handles self-loop")
    void selfLoop() {
        Graph g = new Graph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 1);  // self-loop
        assertTrue(DFS.hasCycleDirected(g));
    }

    @Test
    @DisplayName("hasCycleUndirected detects cycle in loop")
    void cycleUndirected() {
        Graph g = new Graph(4);
        g.addUndirectedEdge(0, 1);
        g.addUndirectedEdge(1, 2);
        g.addUndirectedEdge(2, 3);
        g.addUndirectedEdge(3, 0);  // closes the loop
        assertTrue(DFS.hasCycleUndirected(g));
    }

    @Test
    @DisplayName("hasCycleUndirected returns false on a tree")
    void noCycleUndirected() {
        Graph g = new Graph(5);
        g.addUndirectedEdge(0, 1);
        g.addUndirectedEdge(1, 2);
        g.addUndirectedEdge(2, 3);
        g.addUndirectedEdge(3, 4);
        assertFalse(DFS.hasCycleUndirected(g));
    }

    @Test
    @DisplayName("iterativeDFS visits same vertices as recursive DFS")
    void iterativeMatchesRecursive() {
        Graph g = new Graph(6);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 4);
        g.addEdge(4, 5);
        List<Integer> recursive = DFS.dfsOrder(g, 0);
        List<Integer> iterative  = DFS.iterativeDFS(g, 0);
        assertEquals(recursive.size(), iterative.size());
        // Both should visit all 6 vertices
        assertEquals(6, iterative.size());
    }

    @Test
    @DisplayName("iterativeDFS handles deep chain without stack overflow")
    void deepChain() {
        int n = 50_000;
        Graph g = new Graph(n);
        for (int i = 0; i < n - 1; i++) g.addEdge(i, i + 1);
        List<Integer> result = DFS.iterativeDFS(g, 0);
        assertEquals(n, result.size());
        assertEquals(0, result.get(0));
        assertEquals(n - 1, result.get(n - 1));
    }

    @Test
    @DisplayName("disconnected vertices are not visited from another component")
    void disconnected() {
        Graph g = new Graph(5);
        g.addEdge(0, 1);
        g.addEdge(2, 3);
        // vertex 4 is isolated
        List<Integer> order = DFS.dfsOrder(g, 0);
        assertEquals(2, order.size());
        assertTrue(order.contains(0));
        assertTrue(order.contains(1));
    }
}
