package com.algorithms.graph.bfs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BFSTest {

    private Graph linear;   // 0-1-2-3-4
    private Graph star;     // 0 connected to 1,2,3,4
    private Graph bipartite; // two components: {0,1,2} and {3,4}

    @BeforeEach
    void setUp() {
        linear = new Graph(5);
        for (int i = 0; i < 4; i++) linear.addEdge(i, i + 1);

        star = new Graph(5);
        for (int i = 1; i <= 4; i++) star.addEdge(0, i);

        bipartite = new Graph(5);
        bipartite.addEdge(0, 1);
        bipartite.addEdge(1, 2);
        // 3 and 4 are in a separate component
        bipartite.addEdge(3, 4);
    }

    @Test
    @DisplayName("shortestDistances on linear graph")
    void linearDistances() {
        int[] dist = BFS.shortestDistances(linear, 0);
        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, dist);
    }

    @Test
    @DisplayName("shortestDistances marks unreachable vertices as -1")
    void unreachable() {
        int[] dist = BFS.shortestDistances(bipartite, 0);
        assertEquals( 0, dist[0]);
        assertEquals( 1, dist[1]);
        assertEquals( 2, dist[2]);
        assertEquals(-1, dist[3]);
        assertEquals(-1, dist[4]);
    }

    @Test
    @DisplayName("shortestDistances from center of star: all leaves at distance 1")
    void starDistances() {
        int[] dist = BFS.shortestDistances(star, 0);
        for (int i = 1; i <= 4; i++) assertEquals(1, dist[i]);
    }

    @Test
    @DisplayName("shortestPath returns correct path length")
    void pathLength() {
        List<Integer> path = BFS.shortestPath(linear, 0, 4);
        assertEquals(5, path.size());
        assertEquals(0, path.get(0));
        assertEquals(4, path.get(4));
    }

    @Test
    @DisplayName("shortestPath returns empty list when no path")
    void noPath() {
        List<Integer> path = BFS.shortestPath(bipartite, 0, 3);
        assertTrue(path.isEmpty());
    }

    @Test
    @DisplayName("shortestPath from vertex to itself is just that vertex")
    void selfPath() {
        List<Integer> path = BFS.shortestPath(linear, 2, 2);
        assertEquals(List.of(2), path);
    }

    @Test
    @DisplayName("reachable returns all vertices in connected graph")
    void reachableAll() {
        List<Integer> r = BFS.reachable(linear, 0);
        assertEquals(5, r.size());
    }

    @Test
    @DisplayName("reachable stops at component boundary")
    void reachableComponent() {
        List<Integer> r = BFS.reachable(bipartite, 0);
        assertEquals(3, r.size());
        assertTrue(r.containsAll(List.of(0, 1, 2)));
        assertFalse(r.contains(3));
        assertFalse(r.contains(4));
    }

    @Test
    @DisplayName("BFS path is genuinely shortest (not just any path)")
    void pathIsShortest() {
        // Diamond graph: 0-1, 0-2, 1-3, 2-3
        Graph diamond = new Graph(4);
        diamond.addEdge(0, 1);
        diamond.addEdge(0, 2);
        diamond.addEdge(1, 3);
        diamond.addEdge(2, 3);
        List<Integer> path = BFS.shortestPath(diamond, 0, 3);
        // Either [0,1,3] or [0,2,3] — both length 2 hops
        assertEquals(3, path.size());
        assertEquals(0, path.get(0));
        assertEquals(3, path.get(2));
    }
}
