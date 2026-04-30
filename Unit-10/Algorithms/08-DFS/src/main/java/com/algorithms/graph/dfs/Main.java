package com.algorithms.graph.dfs;

import java.util.List;

/**
 * Demonstrates DFS on a software dependency graph.
 *
 * Modules are vertices; "depends on" relationships are directed edges.
 * DFS detects circular dependencies and explores the full dependency tree.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Depth-First Search — Dependency Analyser ===\n");

        // Module dependency graph (directed)
        // 0=App  1=Auth  2=Database  3=Cache  4=Logger  5=Config  6=Mailer
        String[] modules = {"App", "Auth", "Database", "Cache", "Logger", "Config", "Mailer"};

        Graph deps = new Graph(modules.length);
        deps.addEdge(0, 1);  // App → Auth
        deps.addEdge(0, 2);  // App → Database
        deps.addEdge(1, 2);  // Auth → Database
        deps.addEdge(1, 3);  // Auth → Cache
        deps.addEdge(2, 4);  // Database → Logger
        deps.addEdge(2, 5);  // Database → Config
        deps.addEdge(3, 5);  // Cache → Config
        deps.addEdge(0, 6);  // App → Mailer
        deps.addEdge(6, 4);  // Mailer → Logger

        System.out.println("Module dependencies (A → B means A depends on B):");
        for (String m : modules) System.out.print("  " + m);
        System.out.println();

        List<Integer> dfsOrder = DFS.dfsOrder(deps, 0);
        System.out.print("\nDFS post-order from App: ");
        dfsOrder.stream().map(v -> modules[v]).forEach(m -> System.out.print(m + " "));
        System.out.println("(leaves first — valid build order reversed)");

        boolean cycle = DFS.hasCycleDirected(deps);
        System.out.println("\nCircular dependency detected: " + cycle);

        // Introduce a circular dependency: Config → App
        System.out.println("\nAdding Config → App (circular!)...");
        deps.addEdge(5, 0);
        System.out.println("Circular dependency detected: " + DFS.hasCycleDirected(deps));

        // Iterative DFS on a large tree (avoids stack overflow)
        System.out.println("\n--- Iterative DFS on chain of 10,000 nodes ---");
        Graph chain = new Graph(10_000);
        for (int i = 0; i < 9_999; i++) chain.addEdge(i, i + 1);
        List<Integer> iterResult = DFS.iterativeDFS(chain, 0);
        System.out.println("Nodes visited: " + iterResult.size() + " (first: "
            + iterResult.get(0) + ", last: " + iterResult.get(iterResult.size() - 1) + ")");

        // Undirected cycle detection
        System.out.println("\n--- Undirected cycle detection ---");
        Graph tree = new Graph(5);
        tree.addUndirectedEdge(0, 1);
        tree.addUndirectedEdge(1, 2);
        tree.addUndirectedEdge(2, 3);
        tree.addUndirectedEdge(3, 4);
        System.out.println("Linear tree (no cycle): " + DFS.hasCycleUndirected(tree));
        tree.addUndirectedEdge(4, 0);
        System.out.println("After closing the loop:  " + DFS.hasCycleUndirected(tree));
    }
}
