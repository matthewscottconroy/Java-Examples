package com.markovmonopoly.monopoly.simulation;

import com.markovmonopoly.monopoly.board.MonopolyBoard;

import java.util.List;

/**
 * Accumulates landing counts and transition counts from a Monopoly simulation.
 *
 * <p>After recording enough transitions, call {@link #getEmpiricalTransitionMatrix()}
 * to obtain the empirical (data-derived) Markov chain transition matrix.
 *
 * <p>The state space uses {@link MonopolyBoard#TOTAL_STATES} = 41 states:
 * 0–39 are physical board positions, 40 is the IN_JAIL state.
 */
public class SimulationStats {

    private final int numStates;
    private final long[] landingCounts;
    private final long[][] transitionCounts;
    private long totalTransitions;

    public SimulationStats() {
        this(MonopolyBoard.TOTAL_STATES);
    }

    public SimulationStats(int numStates) {
        this.numStates        = numStates;
        this.landingCounts    = new long[numStates];
        this.transitionCounts = new long[numStates][numStates];
    }

    // -------------------------------------------------------------------------
    // Recording
    // -------------------------------------------------------------------------

    /**
     * Records a transition from state {@code from} to state {@code to}.
     * Also increments the landing count for {@code to}.
     */
    public void recordTransition(int from, int to) {
        transitionCounts[from][to]++;
        landingCounts[to]++;
        totalTransitions++;
    }

    /**
     * Records all transitions from a single turn.
     *
     * @param turnTransitions list of int[]{fromState, toState} pairs
     */
    public void recordTurn(List<int[]> turnTransitions) {
        for (int[] t : turnTransitions) recordTransition(t[0], t[1]);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int  numStates()             { return numStates; }
    public long getTotalTransitions()   { return totalTransitions; }

    public long getLandingCount(int state)           { return landingCounts[state]; }
    public long getTransitionCount(int from, int to) { return transitionCounts[from][to]; }

    /** Empirical visit probability for a state = landings / total transitions. */
    public double getEmpiricalProbability(int state) {
        return totalTransitions == 0 ? 0 : (double) landingCounts[state] / totalTransitions;
    }

    public long[] getLandingCounts() {
        long[] copy = new long[numStates];
        System.arraycopy(landingCounts, 0, copy, 0, numStates);
        return copy;
    }

    // -------------------------------------------------------------------------
    // Matrix derivation
    // -------------------------------------------------------------------------

    /**
     * Computes the empirical transition matrix from recorded transition counts.
     *
     * <p>For each state i with observed outgoing transitions, the matrix entry
     * [i][j] = count(i→j) / count(i→*). For states never visited, the row
     * defaults to a uniform distribution (1/n per state).
     */
    public double[][] getEmpiricalTransitionMatrix() {
        double[][] matrix = new double[numStates][numStates];
        for (int i = 0; i < numStates; i++) {
            long rowTotal = 0;
            for (int j = 0; j < numStates; j++) rowTotal += transitionCounts[i][j];

            if (rowTotal == 0) {
                // Never visited: use uniform (prevents invalid rows in the Markov chain)
                for (int j = 0; j < numStates; j++) matrix[i][j] = 1.0 / numStates;
            } else {
                for (int j = 0; j < numStates; j++) {
                    matrix[i][j] = (double) transitionCounts[i][j] / rowTotal;
                }
            }
        }
        return matrix;
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted table of landing frequencies for each state.
     *
     * @param labels state labels (length must equal numStates)
     */
    public String toFrequencyTable(List<String> labels) {
        if (labels.size() != numStates) {
            throw new IllegalArgumentException("Labels count must equal numStates.");
        }

        long maxCount = 0;
        for (long c : landingCounts) maxCount = Math.max(maxCount, c);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-22s  %9s  %7s  %s%n",
            "Space", "Landings", "Freq %", "Bar"));
        sb.append("-".repeat(70)).append('\n');

        for (int i = 0; i < numStates; i++) {
            double pct = totalTransitions == 0 ? 0
                : 100.0 * landingCounts[i] / totalTransitions;
            int barLen = maxCount == 0 ? 0
                : (int) Math.round(30.0 * landingCounts[i] / maxCount);
            sb.append(String.format("%-22s  %9d  %6.2f%%  %s%n",
                truncate(labels.get(i), 22),
                landingCounts[i], pct,
                "█".repeat(barLen)));
        }
        sb.append(String.format("%nTotal transitions recorded: %,d%n", totalTransitions));
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
