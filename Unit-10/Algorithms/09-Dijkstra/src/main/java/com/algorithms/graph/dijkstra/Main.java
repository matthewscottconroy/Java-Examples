package com.algorithms.graph.dijkstra;

import java.util.List;

/**
 * Demonstrates Dijkstra on a city road network.
 * Vertices are intersections; edges are roads with travel times in minutes.
 */
public class Main {

    static final String[] CITIES = {
        "Downtown", "Airport", "University", "Hospital",
        "Stadium", "Suburb-N", "Suburb-S", "Industrial"
    };

    public static void main(String[] args) {
        System.out.println("=== Dijkstra's Algorithm — City Navigation ===\n");

        WeightedGraph roads = new WeightedGraph(CITIES.length);
        // Undirected roads (travel time in minutes)
        roads.addUndirectedEdge(0, 1, 25);  // Downtown ↔ Airport
        roads.addUndirectedEdge(0, 2, 10);  // Downtown ↔ University
        roads.addUndirectedEdge(0, 3, 15);  // Downtown ↔ Hospital
        roads.addUndirectedEdge(0, 7, 20);  // Downtown ↔ Industrial
        roads.addUndirectedEdge(1, 5,  8);  // Airport ↔ Suburb-N
        roads.addUndirectedEdge(2, 3, 12);  // University ↔ Hospital
        roads.addUndirectedEdge(2, 5, 18);  // University ↔ Suburb-N
        roads.addUndirectedEdge(3, 4, 22);  // Hospital ↔ Stadium
        roads.addUndirectedEdge(4, 6, 14);  // Stadium ↔ Suburb-S
        roads.addUndirectedEdge(5, 6, 30);  // Suburb-N ↔ Suburb-S
        roads.addUndirectedEdge(6, 7, 11);  // Suburb-S ↔ Industrial
        roads.addUndirectedEdge(7, 4, 17);  // Industrial ↔ Stadium

        int source = 0;  // Downtown
        Dijkstra.Result result = Dijkstra.compute(roads, source);

        System.out.println("Shortest travel times from " + CITIES[source] + ":\n");
        System.out.printf("%-15s  %8s  %s%n", "Destination", "Time(min)", "Route");
        System.out.println("-".repeat(60));

        for (int dest = 0; dest < CITIES.length; dest++) {
            if (dest == source) continue;
            int d = result.dist()[dest];
            List<Integer> path = result.path(dest);
            String route = path.stream()
                .map(v -> CITIES[v])
                .reduce((a, b) -> a + " → " + b)
                .orElse("unreachable");
            System.out.printf("%-15s  %8d  %s%n", CITIES[dest], d, route);
        }
    }
}
