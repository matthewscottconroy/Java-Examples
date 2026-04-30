package com.algorithms.graph.dijkstra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraTest {

    @Test
    @DisplayName("single vertex: distance to itself is 0")
    void selfDistance() {
        WeightedGraph g = new WeightedGraph(1);
        int[] dist = Dijkstra.compute(g, 0).dist();
        assertEquals(0, dist[0]);
    }

    @Test
    @DisplayName("linear chain: distances are cumulative edge weights")
    void linearChain() {
        WeightedGraph g = new WeightedGraph(4);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, 7);
        int[] dist = Dijkstra.compute(g, 0).dist();
        assertEquals(0,  dist[0]);
        assertEquals(5,  dist[1]);
        assertEquals(8,  dist[2]);
        assertEquals(15, dist[3]);
    }

    @Test
    @DisplayName("unreachable vertex has MAX_VALUE distance")
    void unreachable() {
        WeightedGraph g = new WeightedGraph(3);
        g.addEdge(0, 1, 10);
        // vertex 2 is isolated
        int[] dist = Dijkstra.compute(g, 0).dist();
        assertEquals(Integer.MAX_VALUE, dist[2]);
    }

    @Test
    @DisplayName("chooses shorter path over direct but heavier edge")
    void choosesShorterPath() {
        //    0 --10--> 2
        //    0 --1-->  1 --1--> 2
        WeightedGraph g = new WeightedGraph(3);
        g.addEdge(0, 2, 10);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        int[] dist = Dijkstra.compute(g, 0).dist();
        assertEquals(2, dist[2]);
    }

    @Test
    @DisplayName("path reconstruction returns correct route")
    void pathReconstruction() {
        WeightedGraph g = new WeightedGraph(4);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(0, 3, 10);  // direct but heavy
        Dijkstra.Result r = Dijkstra.compute(g, 0);
        List<Integer> path = r.path(3);
        assertEquals(List.of(0, 1, 2, 3), path);
        assertEquals(3, r.dist()[3]);
    }

    @Test
    @DisplayName("path to unreachable vertex is empty")
    void unreachablePath() {
        WeightedGraph g = new WeightedGraph(3);
        g.addEdge(0, 1, 5);
        Dijkstra.Result r = Dijkstra.compute(g, 0);
        assertTrue(r.path(2).isEmpty());
    }

    @Test
    @DisplayName("undirected edges work correctly")
    void undirectedEdges() {
        WeightedGraph g = new WeightedGraph(3);
        g.addUndirectedEdge(0, 1, 4);
        g.addUndirectedEdge(1, 2, 6);
        int[] d0 = Dijkstra.compute(g, 0).dist();
        int[] d2 = Dijkstra.compute(g, 2).dist();
        assertEquals(d0[2], d2[0]);  // symmetric
    }

    @Test
    @DisplayName("triangle: picks shortest side")
    void triangle() {
        WeightedGraph g = new WeightedGraph(3);
        g.addUndirectedEdge(0, 1, 2);
        g.addUndirectedEdge(1, 2, 3);
        g.addUndirectedEdge(0, 2, 10);
        int[] dist = Dijkstra.compute(g, 0).dist();
        assertEquals(5, dist[2]);  // 0→1→2 = 5, not direct 0→2 = 10
    }
}
