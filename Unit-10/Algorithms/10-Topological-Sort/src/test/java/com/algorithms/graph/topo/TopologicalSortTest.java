package com.algorithms.graph.topo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortTest {

    /** Returns true if the given order is a valid topological ordering for the adj list. */
    static boolean isValidOrder(List<Integer> order, int n, List<List<Integer>> adj) {
        int[] pos = new int[n];
        for (int i = 0; i < order.size(); i++) pos[order.get(i)] = i;
        for (int u = 0; u < n; u++)
            for (int v : adj.get(u))
                if (pos[u] >= pos[v]) return false;
        return true;
    }

    @Test
    @DisplayName("Kahn: simple chain 0→1→2→3")
    void kahnLinear() {
        List<List<Integer>> adj = TopologicalSort.newAdj(4);
        adj.get(0).add(1); adj.get(1).add(2); adj.get(2).add(3);
        Optional<List<Integer>> result = TopologicalSort.kahn(4, adj);
        assertTrue(result.isPresent());
        assertEquals(List.of(0, 1, 2, 3), result.get());
    }

    @Test
    @DisplayName("Kahn: diamond graph has valid ordering")
    void kahnDiamond() {
        // 0→1, 0→2, 1→3, 2→3
        List<List<Integer>> adj = TopologicalSort.newAdj(4);
        adj.get(0).add(1); adj.get(0).add(2);
        adj.get(1).add(3); adj.get(2).add(3);
        Optional<List<Integer>> result = TopologicalSort.kahn(4, adj);
        assertTrue(result.isPresent());
        assertTrue(isValidOrder(result.get(), 4, adj));
    }

    @Test
    @DisplayName("Kahn: detects cycle")
    void kahnCycle() {
        List<List<Integer>> adj = TopologicalSort.newAdj(3);
        adj.get(0).add(1); adj.get(1).add(2); adj.get(2).add(0);
        assertTrue(TopologicalSort.kahn(3, adj).isEmpty());
    }

    @Test
    @DisplayName("Kahn: single vertex")
    void kahnSingle() {
        List<List<Integer>> adj = TopologicalSort.newAdj(1);
        Optional<List<Integer>> result = TopologicalSort.kahn(1, adj);
        assertTrue(result.isPresent());
        assertEquals(List.of(0), result.get());
    }

    @Test
    @DisplayName("DFS: chain produces valid order")
    void dfsLinear() {
        List<List<Integer>> adj = TopologicalSort.newAdj(4);
        adj.get(0).add(1); adj.get(1).add(2); adj.get(2).add(3);
        Optional<List<Integer>> result = TopologicalSort.dfs(4, adj);
        assertTrue(result.isPresent());
        assertTrue(isValidOrder(result.get(), 4, adj));
    }

    @Test
    @DisplayName("DFS: diamond graph has valid ordering")
    void dfsDiamond() {
        List<List<Integer>> adj = TopologicalSort.newAdj(4);
        adj.get(0).add(1); adj.get(0).add(2);
        adj.get(1).add(3); adj.get(2).add(3);
        Optional<List<Integer>> result = TopologicalSort.dfs(4, adj);
        assertTrue(result.isPresent());
        assertTrue(isValidOrder(result.get(), 4, adj));
    }

    @Test
    @DisplayName("DFS: detects cycle")
    void dfsCycle() {
        List<List<Integer>> adj = TopologicalSort.newAdj(3);
        adj.get(0).add(1); adj.get(1).add(2); adj.get(2).add(0);
        assertTrue(TopologicalSort.dfs(3, adj).isEmpty());
    }

    @Test
    @DisplayName("both algorithms agree on disconnected DAG")
    void disconnectedDAG() {
        // Two independent chains: 0→1, 2→3
        List<List<Integer>> adj = TopologicalSort.newAdj(4);
        adj.get(0).add(1); adj.get(2).add(3);
        Optional<List<Integer>> kahn = TopologicalSort.kahn(4, adj);
        Optional<List<Integer>> dfs  = TopologicalSort.dfs(4, adj);
        assertTrue(kahn.isPresent());
        assertTrue(dfs.isPresent());
        assertTrue(isValidOrder(kahn.get(), 4, adj));
        assertTrue(isValidOrder(dfs.get(),  4, adj));
    }
}
