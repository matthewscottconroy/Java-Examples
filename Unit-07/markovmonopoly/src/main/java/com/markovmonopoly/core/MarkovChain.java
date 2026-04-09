package com.markovmonopoly.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An immutable, named Markov chain with a finite set of states and a
 * {@link TransitionMatrix} governing transitions between them.
 *
 * <p>A Markov chain is a random process where the next state depends only on
 * the current state — not on how we arrived there. This "memoryless" property
 * (the Markov property) makes the chain mathematically tractable and allows
 * exact computation of long-run behavior.
 *
 * <p>Use {@link MarkovAnalysis} for analysis (stationary distribution, mean
 * first passage times, state classification, etc.).
 */
public final class MarkovChain {

    private final String name;
    private final String description;
    private final List<String> stateLabels;
    private final TransitionMatrix matrix;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    /**
     * Creates a Markov chain with the given name, state labels, and transition matrix.
     *
     * @param name        descriptive name (e.g., "Weather Model")
     * @param description optional longer description; may be empty
     * @param labels      ordered list of state names; size must match the matrix
     * @param matrix      row-stochastic transition matrix
     */
    public MarkovChain(String name, String description,
                       List<String> labels, TransitionMatrix matrix) {
        if (labels.size() != matrix.size()) {
            throw new IllegalArgumentException(
                "Number of labels (" + labels.size() +
                ") must match matrix size (" + matrix.size() + ").");
        }
        this.name        = name;
        this.description = description;
        this.stateLabels = Collections.unmodifiableList(new ArrayList<>(labels));
        this.matrix      = matrix;
    }

    /** Convenience factory from arrays. */
    public static MarkovChain of(String name, String[] labels, double[][] matrix) {
        return new MarkovChain(name, "", List.of(labels), TransitionMatrix.of(matrix));
    }

    /** Factory for a chain with both a name and description. */
    public static MarkovChain of(String name, String description,
                                 String[] labels, double[][] matrix) {
        return new MarkovChain(name, description, List.of(labels), TransitionMatrix.of(matrix));
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public int    size()           { return stateLabels.size(); }

    /** Returns an unmodifiable view of the state labels in order. */
    public List<String> getStateLabels() { return stateLabels; }

    /** Returns the label at position {@code index}. */
    public String getLabel(int index) { return stateLabels.get(index); }

    /** Returns the index of the state with the given label, or -1 if absent. */
    public int indexOf(String label) { return stateLabels.indexOf(label); }

    /**
     * Returns the index of the state with the given label.
     * @throws IllegalArgumentException if the label is not found
     */
    public int indexOfOrThrow(String label) {
        int idx = indexOf(label);
        if (idx < 0) throw new IllegalArgumentException("Unknown state: '" + label + "'");
        return idx;
    }

    /** Returns the transition matrix. */
    public TransitionMatrix getMatrix() { return matrix; }

    /** Returns the transition probability from state {@code from} to state {@code to}. */
    public double probability(int from, int to) { return matrix.get(from, to); }

    /** Returns the transition probability from the named from-state to the named to-state. */
    public double probability(String from, String to) {
        return matrix.get(indexOfOrThrow(from), indexOfOrThrow(to));
    }

    /** Returns the row of transition probabilities from state {@code from}. */
    public double[] transitionsFrom(int from) { return matrix.getRow(from); }

    // -------------------------------------------------------------------------
    // Immutable builders
    // -------------------------------------------------------------------------

    /** Returns a copy of this chain with a different name. */
    public MarkovChain withName(String newName) {
        return new MarkovChain(newName, description, stateLabels, matrix);
    }

    /** Returns a copy with the transition probability [from][to] set to {@code value}. */
    public MarkovChain withEntry(int from, int to, double value) {
        return new MarkovChain(name, description, stateLabels, matrix.withEntry(from, to, value));
    }

    /** Returns a copy with each row normalized to sum to 1. */
    public MarkovChain normalized() {
        return new MarkovChain(name, description, stateLabels, matrix.normalized());
    }

    // -------------------------------------------------------------------------
    // Simulation / sampling
    // -------------------------------------------------------------------------

    /**
     * Samples the next state given the current state index.
     *
     * <p>Uses a linear scan of the cumulative probability in row {@code currentState},
     * drawing a uniform random variable to select the next state.
     */
    public int sampleNextState(int currentState, Random rng) {
        double[] row = matrix.getRow(currentState);
        double u = rng.nextDouble();
        double cumulative = 0.0;
        for (int j = 0; j < row.length - 1; j++) {
            cumulative += row[j];
            if (u < cumulative) return j;
        }
        return row.length - 1;  // last state (catches floating-point rounding)
    }

    /** Samples the next state using a thread-local random number generator. */
    public int sampleNextState(int currentState) {
        return sampleNextState(currentState, ThreadLocalRandom.current());
    }

    /** Samples the next state by label. */
    public String sampleNextState(String currentStateLabel) {
        return getLabel(sampleNextState(indexOfOrThrow(currentStateLabel)));
    }

    /**
     * Simulates the chain for {@code steps} steps starting from {@code startState}.
     *
     * @return integer array of length {@code steps + 1} (includes the start state)
     */
    public int[] simulate(int startState, int steps, Random rng) {
        int[] path = new int[steps + 1];
        path[0] = startState;
        for (int t = 1; t <= steps; t++) {
            path[t] = sampleNextState(path[t - 1], rng);
        }
        return path;
    }

    /** Simulates using a thread-local RNG. */
    public int[] simulate(int startState, int steps) {
        return simulate(startState, steps, ThreadLocalRandom.current());
    }

    /**
     * Simulates for {@code steps} steps and returns the empirical distribution
     * (fraction of time spent in each state, not counting the start state).
     */
    public double[] simulateDistribution(int startState, int steps) {
        int[] path = simulate(startState, steps);
        double[] dist = new double[size()];
        for (int t = 1; t < path.length; t++) dist[path[t]]++;
        for (int i = 0; i < dist.length; i++) dist[i] /= steps;
        return dist;
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MarkovChain: ").append(name).append(" (").append(size()).append(" states)\n");
        if (!description.isEmpty()) sb.append("  ").append(description).append("\n");
        sb.append("  States: ").append(stateLabels).append("\n");
        sb.append(matrix);
        return sb.toString();
    }
}
