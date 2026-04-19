package com.epidemic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Agent-based SIR epidemic model running on a {@link WattsStrogatz} small-world network.
 *
 * <h2>Dynamics (per step)</h2>
 * <ol>
 *   <li>For each {@code INFECTED} node, each {@code SUSCEPTIBLE} neighbour becomes
 *       {@code INFECTED} with probability {@code beta}.</li>
 *   <li>Each {@code INFECTED} node independently recovers (transitions to
 *       {@code RECOVERED}) with probability {@code gamma}.</li>
 * </ol>
 *
 * <p>State updates are applied <em>synchronously</em>: all transition decisions
 * for the current step are computed before any state is written, so a node that
 * becomes infected in this step cannot immediately infect others in the same step.
 *
 * <h2>Compartment invariant</h2>
 * <p>{@code S + I + R = N} holds at all times.
 */
public class SIRNetwork {

    private final WattsStrogatz graph;
    private final NodeState[]   states;
    private final int           n;

    /**
     * Construct a network SIR model on the given graph.
     *
     * <p>All nodes start as {@code SUSCEPTIBLE}.  Call {@link #reset(int, Random)}
     * to seed the initial infections before stepping.
     *
     * @param graph the underlying contact network
     */
    public SIRNetwork(WattsStrogatz graph) {
        this.graph  = graph;
        this.n      = graph.nodeCount();
        this.states = new NodeState[n];
        for (int i = 0; i < n; i++) states[i] = NodeState.SUSCEPTIBLE;
    }

    // -------------------------------------------------------------------------
    // Simulation control
    // -------------------------------------------------------------------------

    /**
     * Advance the epidemic by one discrete time step.
     *
     * <p>Infection and recovery decisions are staged into a temporary buffer
     * and committed simultaneously, preserving synchronous update semantics.
     *
     * @param beta  per-contact transmission probability ∈ (0, 1]
     * @param gamma per-step recovery probability ∈ (0, 1]
     * @param rng   random-number generator
     */
    public void step(double beta, double gamma, Random rng) {
        NodeState[] next = states.clone();

        for (int i = 0; i < n; i++) {
            if (states[i] == NodeState.INFECTED) {
                // Attempt to infect each susceptible neighbour
                for (int nb : graph.neighbors(i)) {
                    if (states[nb] == NodeState.SUSCEPTIBLE && rng.nextDouble() < beta) {
                        next[nb] = NodeState.INFECTED;
                    }
                }
                // Attempt recovery
                if (rng.nextDouble() < gamma) {
                    next[i] = NodeState.RECOVERED;
                }
            }
        }

        System.arraycopy(next, 0, states, 0, n);
    }

    /**
     * Reset all nodes to {@code SUSCEPTIBLE} and then seed {@code seedCount}
     * randomly chosen nodes as {@code INFECTED}.
     *
     * @param seedCount number of initially infected nodes (clamped to [1, n])
     * @param rng       random-number generator
     */
    public void reset(int seedCount, Random rng) {
        for (int i = 0; i < n; i++) states[i] = NodeState.SUSCEPTIBLE;

        int clampedSeed = Math.max(1, Math.min(seedCount, n));

        // Shuffle indices and take the first clampedSeed as seeds
        List<Integer> indices = new ArrayList<>(n);
        for (int i = 0; i < n; i++) indices.add(i);
        Collections.shuffle(indices, rng);
        for (int i = 0; i < clampedSeed; i++) {
            states[indices.get(i)] = NodeState.INFECTED;
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the state of node {@code i}.
     *
     * @param i node index in [0, n)
     * @return current {@link NodeState}
     */
    public NodeState getState(int i) {
        return states[i];
    }

    /**
     * Returns the total node count (S + I + R = N).
     *
     * @return number of nodes
     */
    public int getN() {
        return n;
    }

    /**
     * Returns the current count of susceptible nodes.
     *
     * @return number of {@code SUSCEPTIBLE} nodes
     */
    public int getSusceptible() {
        int count = 0;
        for (NodeState s : states) if (s == NodeState.SUSCEPTIBLE) count++;
        return count;
    }

    /**
     * Returns the current count of infected nodes.
     *
     * @return number of {@code INFECTED} nodes
     */
    public int getInfected() {
        int count = 0;
        for (NodeState s : states) if (s == NodeState.INFECTED) count++;
        return count;
    }

    /**
     * Returns the current count of recovered nodes.
     *
     * @return number of {@code RECOVERED} nodes
     */
    public int getRecovered() {
        int count = 0;
        for (NodeState s : states) if (s == NodeState.RECOVERED) count++;
        return count;
    }

    /**
     * Returns the underlying contact graph.
     *
     * @return the {@link WattsStrogatz} graph
     */
    public WattsStrogatz getGraph() {
        return graph;
    }

    /**
     * Returns {@code true} when no infected nodes remain (epidemic has ended).
     *
     * @return {@code true} if the epidemic is over
     */
    public boolean isEpidemicOver() {
        return getInfected() == 0;
    }
}
