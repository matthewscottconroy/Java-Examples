package com.algorithms.graph.topo;

import java.util.List;
import java.util.Optional;

/**
 * Demonstrates topological sort on a university course prerequisite graph.
 *
 * A student cannot enroll in a course until they have completed all prerequisites.
 * Topo sort produces a valid study sequence.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Topological Sort — Course Prerequisite Planner ===\n");

        // Courses (vertices)
        String[] courses = {
            "CS101-Intro",          // 0
            "CS201-Data-Structures",// 1
            "CS202-Algorithms",     // 2
            "CS301-OS",             // 3
            "CS302-Networks",       // 4
            "CS303-Databases",      // 5
            "CS401-Compilers",      // 6
            "CS402-Distributed",    // 7
            "CS403-Security",       // 8
            "MATH101-Calculus",     // 9
            "MATH201-Linear-Alg",   // 10
            "CS501-ML"              // 11
        };

        List<List<Integer>> prereqs = TopologicalSort.newAdj(courses.length);
        // A → B means A is a prerequisite for B
        prereqs.get(0).add(1);   // Intro → Data Structures
        prereqs.get(0).add(9);   // Intro → Calculus
        prereqs.get(1).add(2);   // DS → Algorithms
        prereqs.get(2).add(3);   // Algo → OS
        prereqs.get(2).add(6);   // Algo → Compilers
        prereqs.get(3).add(4);   // OS → Networks
        prereqs.get(3).add(5);   // OS → Databases
        prereqs.get(4).add(7);   // Networks → Distributed
        prereqs.get(5).add(7);   // Databases → Distributed
        prereqs.get(5).add(8);   // Databases → Security
        prereqs.get(9).add(10);  // Calculus → Linear Algebra
        prereqs.get(10).add(11); // Linear Algebra → ML
        prereqs.get(2).add(11);  // Algorithms → ML

        System.out.println("--- Kahn's Algorithm (BFS) ---");
        Optional<List<Integer>> kahn = TopologicalSort.kahn(courses.length, prereqs);
        kahn.ifPresentOrElse(
            order -> {
                System.out.println("Valid study sequence:");
                for (int i = 0; i < order.size(); i++)
                    System.out.printf("  %2d. %s%n", i + 1, courses[order.get(i)]);
            },
            () -> System.out.println("Cycle detected — no valid ordering!")
        );

        System.out.println("\n--- DFS Post-Order ---");
        Optional<List<Integer>> dfsTopo = TopologicalSort.dfs(courses.length, prereqs);
        dfsTopo.ifPresentOrElse(
            order -> {
                System.out.println("Valid study sequence:");
                for (int i = 0; i < order.size(); i++)
                    System.out.printf("  %2d. %s%n", i + 1, courses[order.get(i)]);
            },
            () -> System.out.println("Cycle detected — no valid ordering!")
        );

        // Demonstrate cycle detection
        System.out.println("\n--- Cycle Detection ---");
        List<List<Integer>> cyclic = TopologicalSort.newAdj(4);
        cyclic.get(0).add(1);
        cyclic.get(1).add(2);
        cyclic.get(2).add(0);  // cycle!
        cyclic.get(2).add(3);
        System.out.println("Kahn on cyclic graph: " + (TopologicalSort.kahn(4, cyclic).isEmpty() ? "cycle detected" : "no cycle"));
        System.out.println("DFS  on cyclic graph: " + (TopologicalSort.dfs(4, cyclic).isEmpty()  ? "cycle detected" : "no cycle"));
    }
}
