package com.wattsstrogatz.simulation;

import com.wattsstrogatz.model.*;

import java.util.*;


/**
 * Implements the Watts-Strogatz small-world network model.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Build a ring lattice of n nodes, each connected to k nearest
 *       neighbours on each side (initial degree = 2k).</li>
 *   <li>Visit every edge once, in construction order.</li>
 *   <li>For each edge (u, v), with probability p rewire it: choose a new
 *       target w uniformly at random, avoiding self-loops and multi-edges.
 *       If no valid w exists (graph is already very dense) the edge is left
 *       unchanged.</li>
 * </ol>
 *
 * <p>Stepping one edge at a time lets the UI animate the phase transition and
 * show the "small-world regime" forming as p increases from 0 to 1.
 *
 * <p>Baseline metrics C₀ and L₀ are captured from the initial ring lattice at
 * construction. The normalised ratios C(p)/C₀ and L(p)/L₀ are the two curves
 * reproduced in Figure 2 of the original paper.
 *
 * <p>Reference:
 * <blockquote>Watts, D.J. &amp; Strogatz, S.H. (1998). "Collective dynamics of
 * 'small-world' networks." <i>Nature</i>, 393, 440–442.</blockquote>
 *
 * @see NetworkConfig
 * @see NetworkMetrics
 */
public final class WattsStrogatzSimulation {

    private final NetworkConfig                  config;
    private Network                              network;
    private List<Edge>                           rewireOrder;
    private int                                  currentEdgeIndex;
    private Random                               random;
    private final NetworkMetrics.MetricsSnapshot baseline;

    /**
     * Creates the simulation, builds the initial ring lattice, and records
     * the baseline metrics.
     *
     * @param config simulation parameters; must not be null
     */
    public WattsStrogatzSimulation(NetworkConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.config  = config;
        this.random  = new Random(config.getRandomSeed());
        this.network = Network.ringLattice(config.getNodeCount(), config.getK());
        this.baseline = NetworkMetrics.snapshot(network);
        prepareRewireOrder();
    }

    // -------------------------------------------------------------------------
    // Stepping
    // -------------------------------------------------------------------------

    /**
     * Attempts to rewire the next unvisited edge.
     * With probability p the edge is rewired to a random target; otherwise
     * it is left in place. No-op when rewiring is complete.
     *
     * @return true if there are more edges to visit after this call
     */
    public boolean step() {
        if (isComplete()) return false;
        Edge edge = rewireOrder.get(currentEdgeIndex);
        int  u    = edge.getU();
        if (random.nextDouble() < config.getRewiringProbability()) {
            int w = randomTargetFor(u, edge.getV());
            if (w >= 0) network.rewireEdge(edge, u, w);
        }
        currentEdgeIndex++;
        return !isComplete();
    }

    /**
     * Steps through all remaining edges in one call. After this returns,
     * {@link #isComplete()} is true.
     */
    public void stepAll() {
        while (!isComplete()) step();
    }

    /**
     * Resets the simulation: rebuilds the ring lattice and re-queues all
     * edges. The random seed is reset for reproducibility.
     */
    public void reset() {
        this.random          = new Random(config.getRandomSeed());
        this.network         = Network.ringLattice(config.getNodeCount(), config.getK());
        prepareRewireOrder();
    }

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /** @return true when every edge has been visited */
    public boolean isComplete() { return currentEdgeIndex >= rewireOrder.size(); }

    /** @return the current network (live reference) */
    public Network getNetwork() { return network; }

    /** @return simulation configuration */
    public NetworkConfig getConfig() { return config; }

    /** @return number of edges visited so far */
    public int getEdgesVisited() { return currentEdgeIndex; }

    /** @return total edges to visit */
    public int getTotalEdges() { return rewireOrder.size(); }

    /** @return rewiring progress in [0, 1] */
    public double getProgress() {
        return rewireOrder.isEmpty() ? 1.0 : (double) currentEdgeIndex / rewireOrder.size();
    }

    /**
     * Returns the edge most recently visited by {@link #step()}, or {@code null}
     * if no step has been taken yet. Used by the UI to highlight the active edge
     * during animation.
     *
     * @return last visited edge, or null
     */
    public Edge getLastVisitedEdge() {
        if (currentEdgeIndex == 0) return null;
        return rewireOrder.get(currentEdgeIndex - 1);
    }

    /** @return baseline (ring lattice) metrics recorded at construction */
    public NetworkMetrics.MetricsSnapshot getBaseline() { return baseline; }

    /** @return current raw metrics (C, L) */
    public NetworkMetrics.MetricsSnapshot getCurrentMetrics() {
        return NetworkMetrics.snapshot(network);
    }

    /**
     * Returns relative metrics (C/C0, L/L0) as plotted in the original paper.
     *
     * @return normalised snapshot
     */
    public NetworkMetrics.MetricsSnapshot getRelativeMetrics() {
        return getRelativeMetrics(getCurrentMetrics());
    }

    /**
     * Returns relative metrics using a pre-computed current snapshot, avoiding
     * a redundant O(n²) metrics computation when the caller already has it.
     *
     * @param current already-computed current metrics
     * @return normalised snapshot
     */
    public NetworkMetrics.MetricsSnapshot getRelativeMetrics(NetworkMetrics.MetricsSnapshot current) {
        double relC = baseline.getClusteringCoefficient() == 0.0 ? 1.0
            : current.getClusteringCoefficient() / baseline.getClusteringCoefficient();
        double relL = !Double.isFinite(baseline.getAvgPathLength()) ? 1.0
            : current.getAvgPathLength() / baseline.getAvgPathLength();
        return new NetworkMetrics.MetricsSnapshot(relC, relL);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void prepareRewireOrder() {
        this.rewireOrder      = new ArrayList<>(network.getEdges());
        this.currentEdgeIndex = 0;
    }

    private int randomTargetFor(int u, int currentTarget) {
        int         n         = config.getNodeCount();
        Set<Integer> forbidden = new HashSet<>(network.neighbours(u));
        forbidden.add(u);
        forbidden.add(currentTarget);
        if (forbidden.size() >= n) return -1;
        int attempts = 0;
        while (attempts++ < n * 4) {
            int candidate = random.nextInt(n);
            if (!forbidden.contains(candidate)) return candidate;
        }
        return -1;
    }
}
