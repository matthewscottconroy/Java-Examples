package com.epidemic.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Watts-Strogatz small-world random graph.
 *
 * <h2>Construction algorithm</h2>
 * <ol>
 *   <li>Arrange {@code n} nodes on a ring lattice.  Each node {@code i} is
 *       connected to its {@code k/2} nearest neighbours on each side:
 *       {@code i±1, i±2, …, i±k/2} (indices taken mod n).</li>
 *   <li>For every node {@code i} and for each rightward offset
 *       {@code j = 1 … k/2}: with probability {@code p} rewire the edge
 *       {@code (i, i+j)} by replacing the far endpoint with a uniformly
 *       random node that is neither {@code i} itself nor already a neighbour
 *       of {@code i}.  If no valid target can be found after a bounded search
 *       the edge is left in place.</li>
 * </ol>
 *
 * <p>The resulting graph is undirected and stored as an adjacency-list of
 * {@link HashSet}s so that neighbour look-up, insertion, and removal are all
 * O(1) average-case.
 *
 * <h2>Parameters</h2>
 * <ul>
 *   <li>{@code n} — number of nodes (≥ 3)</li>
 *   <li>{@code k} — mean degree of the ring lattice (even, 2 ≤ k &lt; n)</li>
 *   <li>{@code p} — rewiring probability ∈ [0, 1];
 *       p = 0 gives a regular ring, p = 1 gives a nearly-random graph</li>
 * </ul>
 */
public class WattsStrogatz {

    private final int               n;
    private final List<Set<Integer>> adjacency;

    /**
     * Build a Watts-Strogatz graph directly.
     *
     * @param n   number of nodes
     * @param k   mean degree (must be even and satisfy 2 ≤ k &lt; n)
     * @param p   rewiring probability in [0, 1]
     * @param rng random-number generator used for rewiring
     * @throws IllegalArgumentException if parameters are out of range
     */
    public WattsStrogatz(int n, int k, double p, Random rng) {
        if (n < 3)       throw new IllegalArgumentException("n must be >= 3, got " + n);
        if (k < 2)       throw new IllegalArgumentException("k must be >= 2, got " + k);
        if (k % 2 != 0)  throw new IllegalArgumentException("k must be even, got " + k);
        if (k >= n)      throw new IllegalArgumentException("k must be < n");
        if (p < 0 || p > 1) throw new IllegalArgumentException("p must be in [0,1], got " + p);

        this.n = n;
        this.adjacency = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adjacency.add(new HashSet<>());

        // Step 1: ring lattice
        int half = k / 2;
        for (int i = 0; i < n; i++) {
            for (int d = 1; d <= half; d++) {
                int j = (i + d) % n;
                adjacency.get(i).add(j);
                adjacency.get(j).add(i);
            }
        }

        // Step 2: rewire clockwise edges with probability p
        for (int i = 0; i < n; i++) {
            for (int d = 1; d <= half; d++) {
                if (rng.nextDouble() >= p) continue;

                int j = (i + d) % n;   // current far endpoint

                // Pick a new target that is not i and not already a neighbour of i
                int newTarget = -1;
                for (int attempt = 0; attempt < n * 2; attempt++) {
                    int candidate = rng.nextInt(n);
                    if (candidate != i && !adjacency.get(i).contains(candidate)) {
                        newTarget = candidate;
                        break;
                    }
                }
                if (newTarget == -1) continue;  // no valid target found; keep original edge

                // Remove old edge (i, j)
                adjacency.get(i).remove(j);
                adjacency.get(j).remove(i);

                // Add new edge (i, newTarget)
                adjacency.get(i).add(newTarget);
                adjacency.get(newTarget).add(i);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Convenience factory that seeds its own {@link Random}.
     *
     * @param n    number of nodes
     * @param k    mean degree (even, 2 ≤ k &lt; n)
     * @param p    rewiring probability in [0, 1]
     * @param seed random seed for reproducibility
     * @return a new {@code WattsStrogatz} graph
     */
    public static WattsStrogatz build(int n, int k, double p, long seed) {
        return new WattsStrogatz(n, k, p, new Random(seed));
    }

    // -------------------------------------------------------------------------
    // Graph accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the number of nodes in the graph.
     *
     * @return node count
     */
    public int nodeCount() {
        return n;
    }

    /**
     * Returns an unmodifiable view of the neighbours of node {@code i}.
     *
     * @param i node index in [0, n)
     * @return set of neighbour indices
     * @throws IndexOutOfBoundsException if {@code i} is out of range
     */
    public Set<Integer> neighbors(int i) {
        return adjacency.get(i);
    }
}
