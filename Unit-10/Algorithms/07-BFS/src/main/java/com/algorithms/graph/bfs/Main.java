package com.algorithms.graph.bfs;

import java.util.List;

/**
 * Demonstrates BFS on an office floor-plan navigation problem.
 *
 * Vertices are rooms; edges are doorways. BFS finds the shortest path
 * (fewest doors to pass through) between any two rooms.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Breadth-First Search — Office Navigation ===\n");

        // Office layout (14 rooms, 0-indexed)
        // 0=Lobby  1=Reception  2=Conf-A  3=Conf-B  4=Cafe  5=Open-Office
        // 6=Dev-1  7=Dev-2  8=Dev-3  9=Server-Room  10=Mgmt
        // 11=HR    12=Finance  13=Exit
        String[] rooms = {
            "Lobby", "Reception", "Conf-A", "Conf-B", "Cafe", "Open-Office",
            "Dev-1", "Dev-2", "Dev-3", "Server-Room", "Management",
            "HR", "Finance", "Exit"
        };

        Graph office = new Graph(rooms.length);
        // Corridors between rooms
        int[][] edges = {
            {0, 1}, {0, 4}, {1, 2}, {1, 3}, {1, 5},
            {4, 5}, {5, 6}, {5, 7}, {5, 8}, {6, 9},
            {7, 9}, {5, 10}, {10, 11}, {10, 12}, {4, 13}
        };
        for (int[] e : edges) office.addEdge(e[0], e[1]);

        System.out.println("Rooms: " + String.join(", ",
            java.util.Arrays.stream(rooms).toList()));

        // Shortest path: Lobby → Server-Room
        int start = 0, end = 9;
        List<Integer> path = BFS.shortestPath(office, start, end);
        System.out.printf("%nShortest path from %s to %s:%n", rooms[start], rooms[end]);
        path.forEach(v -> System.out.println("  → " + rooms[v]));
        System.out.println("  (" + (path.size() - 1) + " doorways)");

        // All distances from Lobby
        int[] dist = BFS.shortestDistances(office, 0);
        System.out.println("\nDistance from Lobby to every room:");
        for (int i = 0; i < rooms.length; i++) {
            System.out.printf("  %-15s  %d hops%n", rooms[i], dist[i]);
        }

        // Reachable from Cafe (check connectivity)
        System.out.println("\nRooms reachable from Cafe (in BFS order):");
        List<Integer> reachable = BFS.reachable(office, 4);
        reachable.forEach(v -> System.out.println("  " + rooms[v]));
    }
}
