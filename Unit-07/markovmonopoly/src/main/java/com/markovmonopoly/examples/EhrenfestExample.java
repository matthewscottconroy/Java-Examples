package com.markovmonopoly.examples;

import com.markovmonopoly.core.MarkovAnalysis;
import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.core.TransitionMatrix;
import com.markovmonopoly.ui.TableFormatter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the <b>Ehrenfest Urn Model</b> — a classic birth-death chain.
 *
 * <h2>Physical Setup</h2>
 * <p>Imagine 2N balls distributed between two urns (A and B). At each step,
 * one ball is chosen uniformly at random and moved to the other urn.
 *
 * <p>The state is the number of balls in urn A: k ∈ {0, 1, 2, ..., 2N}.
 *
 * <p>Transition probabilities:
 * <ul>
 *   <li>From state k: go to k-1 with probability k/(2N)  (pick a ball from A)</li>
 *   <li>From state k: go to k+1 with probability (2N-k)/(2N)  (pick a ball from B)</li>
 * </ul>
 *
 * <h2>Historical Significance</h2>
 * <p>Paul and Tatiana Ehrenfest (1907) introduced this model to explain the
 * irreversibility paradox in statistical mechanics: why does a gas mix uniformly
 * even though the underlying physics is reversible?
 *
 * <h2>What This Example Shows</h2>
 * <ol>
 *   <li>Birth-death chains: transitions only to adjacent states</li>
 *   <li>Detailed balance (time-reversibility): π[k]·P[k][k+1] = π[k+1]·P[k+1][k]</li>
 *   <li>Stationary distribution = Binomial(2N, 1/2): the "most mixed" state is most likely</li>
 *   <li>Periodicity: the chain has period 2 (parity alternates)</li>
 *   <li>Convergence to equilibrium and the arrow of time</li>
 * </ol>
 */
public final class EhrenfestExample {

    private EhrenfestExample() {}

    private static final int TWO_N = 10;  // total balls (2N)

    public static void run(PrintStream out) {
        out.println(TableFormatter.sectionHeader("EXAMPLE 4: EHRENFEST URN MODEL"));
        out.println("Setup: " + TWO_N + " balls in two urns. State = # balls in urn A (0.." + TWO_N + ").");
        out.println("Each step: pick a random ball and move it to the other urn.");
        out.println();
        out.println("This models diffusion/mixing. A gas expanding to fill a room is similar:");
        out.println("the system naturally evolves toward the most probable (most mixed) state.");
        out.println();

        MarkovChain chain = buildEhrenfest(TWO_N);

        // ── Transition matrix (partial) ────────────────────────────────────────
        out.println(TableFormatter.subHeader("TRANSITION PROBABILITIES (birth-death structure)"));
        out.println("  Only adjacent states can transition (pure birth-death chain):");
        out.println();
        out.printf("  %-6s  %-20s  %-20s%n", "State", "P(→k-1) [ball from A]", "P(→k+1) [ball from B]");
        out.println("  " + "-".repeat(50));
        for (int k = 0; k <= TWO_N; k++) {
            double pDown = (double) k / TWO_N;
            double pUp   = (double) (TWO_N - k) / TWO_N;
            out.printf("  k=%-4d  %-20.4f  %-20.4f%n", k, pDown, pUp);
        }
        out.println();

        // ── Stationary distribution ────────────────────────────────────────────
        out.println(TableFormatter.subHeader("STATIONARY DISTRIBUTION"));
        out.println("""
            The stationary distribution is Binomial(2N, 1/2): the most mixed state
            (equal balls in each urn) is the most likely. This explains why we
            never see a gas spontaneously unmix: the probability is astronomically small.
            """);

        double[] pi = MarkovAnalysis.stationaryDistribution(chain);
        List<String> labels = new ArrayList<>();
        for (int k = 0; k <= TWO_N; k++) labels.add("k=" + k);
        out.print(TableFormatter.formatDistribution(pi, labels));
        out.println();

        // Verify it's binomial
        out.println("  Theoretical Binomial(2N=10, p=1/2) vs. computed stationary:");
        double total = Math.pow(2, TWO_N);
        out.printf("  %-6s  %-14s  %-14s%n", "State", "Computed", "Binomial");
        for (int k = 0; k <= TWO_N; k++) {
            double binomial = binomialCoeff(TWO_N, k) / total;
            out.printf("  k=%-4d  %-14.6f  %-14.6f%n", k, pi[k], binomial);
        }
        out.println();

        // ── Detailed balance ───────────────────────────────────────────────────
        out.println(TableFormatter.subHeader("DETAILED BALANCE (Time-Reversibility)"));
        out.println("""
            A chain satisfies detailed balance if: π[i]·P[i][j] = π[j]·P[j][i]
            for all pairs i,j. This means the chain looks the same forwards and
            backwards in time — it is time-reversible.
            """);
        out.printf("  %-6s  %-6s  %-16s  %-16s  %-10s%n",
            "k", "k+1", "π[k]·P[k→k+1]", "π[k+1]·P[k+1→k]", "Balanced?");
        out.println("  " + "-".repeat(60));

        double[][] matrix = chain.getMatrix().toArray();
        for (int k = 0; k < TWO_N; k++) {
            double lhs = pi[k] * matrix[k][k + 1];
            double rhs = pi[k + 1] * matrix[k + 1][k];
            boolean balanced = Math.abs(lhs - rhs) < 1e-9;
            out.printf("  %-6d  %-6d  %-16.8f  %-16.8f  %-10s%n",
                k, k + 1, lhs, rhs, balanced ? "✓ YES" : "✗ NO");
        }
        out.println();
        out.println("  All pairs satisfy detailed balance → chain is time-reversible.");

        // ── Periodicity ────────────────────────────────────────────────────────
        out.println(TableFormatter.subHeader("PERIODICITY"));
        out.println("""
            The Ehrenfest chain has period 2: from any state k, the parity of
            the state flips each step (you always move to k±1). This means:
            - You can return to state k only in an even number of steps.
            - The chain is NOT aperiodic → it does not converge to a unique stationary
              distribution from all starting points.
            """);

        int period = MarkovAnalysis.period(chain, TWO_N / 2);
        out.printf("  Period of state k=%d: %d%n", TWO_N / 2, period);
        out.printf("  Aperiodic: %b%n%n", MarkovAnalysis.isAperiodic(chain));

        // ── Convergence from extreme state ────────────────────────────────────
        out.println(TableFormatter.subHeader("EVOLUTION FROM ALL BALLS IN URN A (state " + TWO_N + ")"));
        out.println("  Starting from k=10 (all balls in A), watch the distribution evolve:");
        out.println();
        out.printf("  %-6s  %-12s  %-12s  %-12s%n", "Steps", "P(k=5±1)", "P(k=0 or 10)", "TV Dist");
        out.println("  " + "-".repeat(46));

        int[] checkpoints = {0, 2, 4, 6, 10, 20, 50, 100};
        for (int steps : checkpoints) {
            double[] dist = MarkovAnalysis.distributionAfterSteps(chain, TWO_N, steps);
            double nearMid = dist[4] + dist[5] + dist[6];
            double extremes = dist[0] + dist[TWO_N];
            double tv = MarkovAnalysis.totalVariationDistance(dist, pi);
            out.printf("  %-6d  %-12.4f  %-12.6f  %-12.6f%n",
                steps, nearMid, extremes, tv);
        }
        out.println();
        out.println("""
            KEY INSIGHT: The system evolves irreversibly toward the mixed state
            (k ≈ 5) even though each individual step is reversible. The probability
            of returning to k=10 (completely unmixed) becomes vanishingly small —
            this is the statistical explanation of the Second Law of Thermodynamics.
            """);
    }

    private static MarkovChain buildEhrenfest(int totalBalls) {
        int n = totalBalls + 1;
        double[][] matrix = new double[n][n];

        // State k: k balls in urn A
        matrix[0][1] = 1.0;                      // from k=0: can only go up
        matrix[totalBalls][totalBalls - 1] = 1.0; // from k=N: can only go down

        for (int k = 1; k < totalBalls; k++) {
            matrix[k][k - 1] = (double) k / totalBalls;
            matrix[k][k + 1] = (double) (totalBalls - k) / totalBalls;
        }

        List<String> labels = new ArrayList<>();
        for (int k = 0; k <= totalBalls; k++) labels.add("k=" + k);

        return new MarkovChain(
            "Ehrenfest Urn (2N=" + totalBalls + ")",
            "Birth-death chain modeling diffusion. Stationary dist = Binomial(2N, 1/2).",
            labels, TransitionMatrix.of(matrix)
        );
    }

    private static double binomialCoeff(int n, int k) {
        if (k < 0 || k > n) return 0;
        double result = 1;
        for (int i = 0; i < k; i++) {
            result *= (n - i);
            result /= (i + 1);
        }
        return result;
    }
}
