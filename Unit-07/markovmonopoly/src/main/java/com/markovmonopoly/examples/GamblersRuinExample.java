package com.markovmonopoly.examples;

import com.markovmonopoly.core.MarkovAnalysis;
import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.core.StateClass;
import com.markovmonopoly.core.TransitionMatrix;
import com.markovmonopoly.ui.TableFormatter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates the classic <b>Gambler's Ruin</b> problem as an absorbing Markov chain.
 *
 * <h2>Problem Setup</h2>
 * <p>A gambler starts with $k and plays a sequence of bets. Each bet wins $1
 * with probability p, and loses $1 with probability q = 1-p. The game ends when
 * the gambler is ruined ($0) or reaches the target ($N). Both are absorbing states.
 *
 * <h2>What This Example Shows</h2>
 * <ol>
 *   <li>Absorbing Markov chains and how to identify absorbing states</li>
 *   <li>The fundamental matrix N = (I-Q)⁻¹</li>
 *   <li>Absorption probabilities: analytical formula vs. Markov chain computation</li>
 *   <li>Expected game duration from each starting fortune</li>
 *   <li>How changing the win probability (fair vs. unfair game) affects outcomes</li>
 * </ol>
 *
 * <h2>Analytical Result</h2>
 * <p>Probability of reaching $N starting from $k:
 * <ul>
 *   <li>Fair game (p = 1/2): P(win | start $k) = k/N</li>
 *   <li>Biased game (p ≠ 1/2): P(win | start $k) = (1 - (q/p)^k) / (1 - (q/p)^N)</li>
 * </ul>
 */
public final class GamblersRuinExample {

    private GamblersRuinExample() {}

    private static final int N = 8;   // target fortune ($8)

    /** Returns the fair Gambler's Ruin chain (p=0.5) without console output. */
    public static MarkovChain buildChain() {
        return buildGamblersRuin(N, 0.5);
    }

    public static void run(PrintStream out) {
        out.println(TableFormatter.sectionHeader("EXAMPLE 2: GAMBLER'S RUIN"));
        out.println("""
            Setup: A gambler starts with some amount $k (0 < k < N).
            Each turn: win $1 (prob p) or lose $1 (prob q = 1-p).
            Game ends when fortune reaches $0 (ruined) or $N (target).

            States: 0, 1, 2, ..., N
            Absorbing states: 0 (ruin) and N (success)
            Transient states: 1, 2, ..., N-1
            """);

        runScenario(out, 0.5, "FAIR GAME (p = 0.5, equal odds)");
        runScenario(out, 0.4, "UNFAIR GAME (p = 0.4, casino has edge)");
        runScenario(out, 0.6, "FAVORABLE GAME (p = 0.6, player has edge)");
    }

    private static void runScenario(PrintStream out, double p, String title) {
        double q = 1.0 - p;
        out.println(TableFormatter.subHeader(title));

        MarkovChain chain = buildGamblersRuin(N, p);
        List<Integer> tStates = MarkovAnalysis.transientStates(chain);
        List<Integer> absorbing = MarkovAnalysis.absorbingStates(chain);

        out.printf("  States 0 and %d are ABSORBING (game over).%n", N);
        out.printf("  States 1..%d are TRANSIENT (game in progress).%n%n", N - 1);

        // Absorption probabilities
        double[][] B = MarkovAnalysis.absorptionProbabilities(chain);
        double[] expectedTime = MarkovAnalysis.expectedStepsToAbsorption(chain);

        out.printf("  %-8s  %-18s  %-18s  %-12s  %-18s%n",
            "Start $k", "P(ruin at $0)", "P(win at $" + N + ")", "Analytical", "Expected turns");
        out.println("  " + "-".repeat(80));

        for (int r = 0; r < tStates.size(); r++) {
            int k = tStates.get(r);  // starting fortune
            double pRuinMC    = B[r][0];  // absorption to state 0 (ruin)
            double pWinMC     = B[r][1];  // absorption to state N (win)
            double analytical = analyticalWinProb(k, N, p, q);
            double expected   = expectedTime[r];

            out.printf("  $%-7d  %-18.6f  %-18.6f  %-12.6f  %-18.2f%n",
                k, pRuinMC, pWinMC, analytical, expected);
        }
        out.println();

        // Interpretation
        int midpoint = N / 2;
        int midIdx = tStates.indexOf(midpoint);
        if (midIdx >= 0) {
            out.printf("  At $%d (half the target):%n", midpoint);
            out.printf("  Chance of winning: %.1f%%%n", B[midIdx][1] * 100);
            out.printf("  Expected game length: %.1f turns%n%n", expectedTime[midIdx]);
        }

        if (Math.abs(p - 0.5) < 1e-9) {
            out.println("  KEY INSIGHT (Fair Game): The win probability k/N is exactly proportional");
            out.println("  to your starting fortune as a fraction of the target. With $4 of $8,");
            out.println("  you have exactly a 50% chance — the game is symmetric.");
        } else if (p < 0.5) {
            out.printf("  KEY INSIGHT (Casino Edge): With p=%.1f, even starting at $%d of $%d,%n",
                p, N - 1, N);
            out.printf("  your win probability is only %.1f%%. The house edge compounds badly.%n",
                B[tStates.indexOf(N - 1)][1] * 100);
        } else {
            out.printf("  KEY INSIGHT (Player Edge): With p=%.1f, starting at $1 of $%d,%n", p, N);
            out.printf("  you still have a %.1f%% chance to reach $%d.%n",
                B[0][1] * 100, N);
        }
        out.println();
    }

    /** Builds the Gambler's Ruin chain with N+1 states (0..N) and win prob p. */
    public static MarkovChain buildGamblersRuin(int N, double p) {
        double q = 1.0 - p;
        int n = N + 1;
        double[][] matrix = new double[n][n];

        // Absorbing states: 0 and N
        matrix[0][0] = 1.0;
        matrix[N][N] = 1.0;

        // Transient states: 1..N-1
        for (int k = 1; k < N; k++) {
            matrix[k][k - 1] = q;
            matrix[k][k + 1] = p;
        }

        List<String> labels = new ArrayList<>();
        for (int i = 0; i <= N; i++) labels.add("$" + i);

        return new MarkovChain(
            String.format("Gambler's Ruin (N=%d, p=%.2f)", N, p),
            "Classic absorbing chain. States 0 and N are absorbing.",
            labels, TransitionMatrix.of(matrix)
        );
    }

    private static double analyticalWinProb(int k, int N, double p, double q) {
        if (Math.abs(p - 0.5) < 1e-9) {
            return (double) k / N;  // fair game
        }
        double r = q / p;
        return (1.0 - Math.pow(r, k)) / (1.0 - Math.pow(r, N));
    }
}
