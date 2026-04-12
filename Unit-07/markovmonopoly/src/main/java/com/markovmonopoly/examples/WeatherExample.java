package com.markovmonopoly.examples;

import com.markovmonopoly.core.MarkovAnalysis;
import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.core.StateClass;
import com.markovmonopoly.ui.TableFormatter;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates a simple 3-state weather Markov chain — the classic introductory example.
 *
 * <h2>Concept: The Markov Property</h2>
 * <p>A Markov chain is a random process where the future depends only on the
 * present state, not on the history. Weather is a natural example: tomorrow's
 * weather depends on today's, not on the whole past week's.
 *
 * <h2>What This Example Shows</h2>
 * <ol>
 *   <li>How to define a Markov chain from a transition matrix</li>
 *   <li>The stationary distribution (long-run frequencies)</li>
 *   <li>Convergence: starting from any state, the distribution converges to stationary</li>
 *   <li>Mean first passage times: how long until we first see each weather type</li>
 *   <li>State classification: all states are recurrent in this ergodic chain</li>
 * </ol>
 */
public final class WeatherExample {

    private WeatherExample() {}

    /** Transition matrix: rows = from, cols = to. */
    private static final double[][] TRANSITIONS = {
        // Sunny  Cloudy  Rainy
        {  0.70,   0.20,  0.10 },   // from Sunny
        {  0.30,   0.40,  0.30 },   // from Cloudy
        {  0.20,   0.30,  0.50 },   // from Rainy
    };

    private static final String[] STATES = { "Sunny", "Cloudy", "Rainy" };

    /** Returns the Weather Model chain without producing any console output. */
    public static MarkovChain buildChain() {
        return MarkovChain.of(
            "Weather Model",
            "3-state Markov chain modeling daily weather patterns.",
            STATES, TRANSITIONS);
    }

    public static void run(PrintStream out) {
        MarkovChain chain = MarkovChain.of(
            "Weather Model",
            "3-state Markov chain modeling daily weather patterns.",
            STATES, TRANSITIONS
        );

        out.println(TableFormatter.sectionHeader("EXAMPLE 1: THE WEATHER MODEL"));
        out.println("""
            A Markov chain models a system that moves between a finite set of states
            randomly. The key property is memorylessness: the next state depends only
            on the current state, not on the history of past states.

            Here we model daily weather as a 3-state Markov chain:
              Sunny, Cloudy, Rainy

            The transition matrix P[i][j] gives the probability of moving from
            state i (row) to state j (column) in one step.
            """);

        // ── Transition matrix ──────────────────────────────────────────────────
        out.println(TableFormatter.subHeader("TRANSITION MATRIX"));
        out.println("  P[i][j] = probability of going from state i (row) to state j (column)");
        out.println();
        out.print(TableFormatter.formatMatrix(TRANSITIONS, List.of(STATES)));

        // ── Stationary distribution ────────────────────────────────────────────
        out.println(TableFormatter.subHeader("STATIONARY DISTRIBUTION (Long-run Behavior)"));
        out.println("""
            The stationary distribution π satisfies: π = π·P
            It represents the fraction of time the chain spends in each state
            in the long run, regardless of where it started.
            Computed via power iteration: start from uniform, repeatedly apply P.
            """);

        double[] pi = MarkovAnalysis.stationaryDistribution(chain);
        out.print(TableFormatter.formatDistribution(pi, List.of(STATES)));
        out.printf("%n  Interpretation: In the long run, this city is sunny %.1f%% of days.%n%n",
            pi[0] * 100);

        // ── Convergence ────────────────────────────────────────────────────────
        out.println(TableFormatter.subHeader("CONVERGENCE TO STATIONARY DISTRIBUTION"));
        out.println("""
            Starting from a pure state (100% Sunny), watch the distribution evolve.
            This demonstrates convergence to the stationary distribution.
            """);
        out.printf("  %-8s  %-10s  %-10s  %-10s  %-12s%n",
            "Steps", "Sunny", "Cloudy", "Rainy", "TV Distance");
        out.println("  " + "-".repeat(58));

        int[] checkpoints = {0, 1, 2, 3, 5, 10, 20, 50};
        for (int steps : checkpoints) {
            double[] dist = MarkovAnalysis.distributionAfterSteps(chain, 0, steps);
            double tv = MarkovAnalysis.totalVariationDistance(dist, pi);
            out.printf("  %-8d  %-10.4f  %-10.4f  %-10.4f  %-12.6f%n",
                steps, dist[0], dist[1], dist[2], tv);
        }
        out.println();
        out.println("  Note: Total Variation (TV) distance measures how far the");
        out.println("  distribution is from stationary. It approaches 0 as steps → ∞.");
        out.println();

        // ── Mean first passage times ───────────────────────────────────────────
        out.println(TableFormatter.subHeader("MEAN FIRST PASSAGE TIMES"));
        out.println("""
            m[i][j] = expected number of steps to reach state j for the first time,
            starting from state i.  Diagonal m[i][i] = mean recurrence time = 1/π[i].
            """);

        double[][] mfpt = MarkovAnalysis.meanFirstPassageTimes(chain);
        out.print(TableFormatter.formatMatrix(mfpt, List.of(STATES)));

        out.println();
        out.printf("  Interpretation: Starting from Rainy, it takes on average %.1f days%n",
            mfpt[chain.indexOf("Rainy")][chain.indexOf("Sunny")]);
        out.printf("  to see the first Sunny day. The mean recurrence time for Rainy%n");
        out.printf("  is %.1f days (= 1/π[Rainy] = 1/%.4f).%n%n",
            mfpt[chain.indexOf("Rainy")][chain.indexOf("Rainy")], pi[chain.indexOf("Rainy")]);

        // ── State classification ───────────────────────────────────────────────
        out.println(TableFormatter.subHeader("STATE CLASSIFICATION"));
        Map<Integer, StateClass> classes = MarkovAnalysis.classifyAllStates(chain);
        for (int i = 0; i < chain.size(); i++) {
            out.printf("  %-8s → %s%n", chain.getLabel(i), classes.get(i));
        }
        out.println();
        out.printf("  Irreducible: %b   Aperiodic: %b   Ergodic: %b%n",
            MarkovAnalysis.isIrreducible(chain),
            MarkovAnalysis.isAperiodic(chain),
            MarkovAnalysis.isErgodic(chain));
        out.println("""

            All three states are RECURRENT — once the chain visits any state,
            it is guaranteed to return to it infinitely often.

            The chain is ERGODIC (irreducible + aperiodic), which means:
              1. Every state is reachable from every other state.
              2. There is a unique stationary distribution.
              3. The distribution converges to π from any starting point.
            """);

        // ── Simulation ────────────────────────────────────────────────────────
        out.println(TableFormatter.subHeader("SIMULATION (100 steps from Sunny)"));
        int[] path = chain.simulate(0, 100);
        double[] empDist = new double[3];
        for (int t = 1; t <= 100; t++) empDist[path[t]]++;
        for (int i = 0; i < 3; i++) empDist[i] /= 100;

        out.println("  Empirical frequencies from 100 simulated steps:");
        for (int i = 0; i < 3; i++) {
            out.printf("  %-8s  simulated=%.3f  theoretical=%.3f%n",
                STATES[i], empDist[i], pi[i]);
        }
        out.println();
        out.println("  (Run more steps for the empirical frequencies to converge)");
    }
}
