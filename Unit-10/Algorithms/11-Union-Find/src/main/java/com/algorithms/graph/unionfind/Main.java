package com.algorithms.graph.unionfind;

/**
 * Demonstrates Union-Find on a network connectivity problem.
 *
 * A data centre starts with isolated servers. Cables are installed one by one.
 * Union-Find instantly answers "are server A and server B connected?" after
 * each cable is added, without re-scanning the entire network.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Union-Find — Network Connectivity ===\n");

        int servers = 10;
        UnionFind uf = new UnionFind(servers);

        System.out.println("Servers: 0 through " + (servers - 1));
        System.out.println("Initially: " + uf.componentCount() + " isolated components\n");

        // Install cables one by one
        int[][] cables = {
            {0, 1}, {2, 3}, {4, 5}, {6, 7}, {8, 9},
            {0, 2}, {4, 6}, {0, 4}, {1, 8}
        };

        System.out.printf("%-20s  %-10s  %s%n", "Cable installed", "Merged?", "Components");
        System.out.println("-".repeat(50));

        for (int[] cable : cables) {
            boolean merged = uf.union(cable[0], cable[1]);
            System.out.printf("%-20s  %-10s  %d%n",
                cable[0] + " ↔ " + cable[1], merged, uf.componentCount());
        }

        System.out.println("\nConnectivity queries:");
        int[][] queries = {{0, 9}, {0, 3}, {5, 7}, {3, 9}};
        for (int[] q : queries) {
            System.out.printf("  Server %d ↔ Server %d: %s%n",
                q[0], q[1], uf.connected(q[0], q[1]) ? "CONNECTED" : "isolated");
        }

        // Kruskal's MST: add edges in weight order, skip if both endpoints already connected
        System.out.println("\n--- Kruskal's Minimum Spanning Tree ---");
        // Edges: [from, to, weight]
        int[][] edges = {
            {0,1,4}, {0,2,3}, {1,3,2}, {2,3,6}, {1,4,5},
            {3,4,1}, {2,4,7}
        };
        // Sort by weight
        java.util.Arrays.sort(edges, (a, b) -> Integer.compare(a[2], b[2]));

        UnionFind mst = new UnionFind(5);
        int totalWeight = 0;
        System.out.println("Edges in MST:");
        for (int[] e : edges) {
            if (mst.union(e[0], e[1])) {
                System.out.printf("  %d — %d  (weight %d)%n", e[0], e[1], e[2]);
                totalWeight += e[2];
            }
        }
        System.out.println("Total MST weight: " + totalWeight);
    }
}
