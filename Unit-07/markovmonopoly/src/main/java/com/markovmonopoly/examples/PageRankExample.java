package com.markovmonopoly.examples;

import com.markovmonopoly.core.MarkovAnalysis;
import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.core.TransitionMatrix;
import com.markovmonopoly.ui.TableFormatter;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates <b>PageRank</b> as a Markov chain stationary distribution.
 *
 * <h2>The Idea</h2>
 * <p>Google's original PageRank algorithm models a "random web surfer" as a
 * Markov chain. At each step, the surfer either:
 * <ul>
 *   <li>Follows one of the current page's outgoing links (probability 1-α), or</li>
 *   <li>Teleports to a uniformly random page (probability α)</li>
 * </ul>
 * The stationary distribution of this chain is the PageRank of each page —
 * pages that many high-ranked pages link to have high PageRank.
 *
 * <h2>Why Teleportation?</h2>
 * <p>Without teleportation, "dangling nodes" (pages with no outgoing links)
 * or disconnected groups would make the chain non-ergodic and the stationary
 * distribution non-unique. Teleportation (damping factor α) ensures the chain
 * is ergodic.
 *
 * <h2>What This Example Shows</h2>
 * <ol>
 *   <li>Building a Markov chain from a directed graph (adjacency matrix)</li>
 *   <li>The PageRank formula and damping factor</li>
 *   <li>How stationary distribution = importance ranking</li>
 *   <li>Effect of different damping factors on the ranking</li>
 * </ol>
 */
public final class PageRankExample {

    private PageRankExample() {}

    /**
     * Adjacency matrix for a small 6-page web graph.
     * A[i][j] = 1 means page i links to page j.
     *
     * Graph structure:
     *   Home (0) → About(1), Blog(2), Products(3)
     *   About (1) → Home(0)
     *   Blog (2) → Home(0), About(1), Post1(4), Post2(5)
     *   Products (3) → Home(0), About(1)
     *   Post1 (4) → Blog(2)
     *   Post2 (5) → Blog(2), Post1(4)
     */
    private static final int[][] LINKS = {
        {0, 1, 1, 1, 0, 0},  // Home
        {1, 0, 0, 0, 0, 0},  // About
        {1, 1, 0, 0, 1, 1},  // Blog
        {1, 1, 0, 0, 0, 0},  // Products
        {0, 0, 1, 0, 0, 0},  // Post1
        {0, 0, 1, 1, 0, 0},  // Post2
    };

    private static final String[] PAGES = {
        "Home", "About", "Blog", "Products", "Post1", "Post2"
    };

    public static void run(PrintStream out) {
        out.println(TableFormatter.sectionHeader("EXAMPLE 3: PAGERANK"));
        out.println("""
            Google's PageRank (1998) ranks web pages by modeling a random surfer.
            The surfer follows links randomly, occasionally "teleporting" to any page.
            The stationary distribution of this Markov chain is the page's rank.

            Web graph (6 pages):
            """);

        // Print link structure
        for (int i = 0; i < 6; i++) {
            StringBuilder links = new StringBuilder();
            for (int j = 0; j < 6; j++) {
                if (LINKS[i][j] == 1) {
                    if (links.length() > 0) links.append(", ");
                    links.append(PAGES[j]);
                }
            }
            out.printf("  %-10s → %s%n", PAGES[i], links);
        }
        out.println();

        runWithDampingFactor(out, 0.15, "STANDARD PAGERANK (α=0.15, Google's original value)");
        runWithDampingFactor(out, 0.50, "HIGH TELEPORTATION (α=0.50, flattens rankings)");
        runWithDampingFactor(out, 0.01, "LOW TELEPORTATION (α=0.01, pure link structure)");

        out.println("""
            KEY INSIGHTS:
            1. "Blog" has high PageRank because it is linked-to by 4 pages.
            2. "Home" has high PageRank because it is linked-to from 4 pages (including Blog).
            3. "Post1" and "Post2" have low PageRank — they are leaf nodes with few inbound links.
            4. Higher α (more teleportation) → rankings become more uniform.
            5. Lower α (fewer teleportation) → link structure dominates completely.

            The key formula:
              PR_final = α·(1/n vector) + (1-α)·(link-based transition)·PR_current
            This is just one step of multiplying the probability vector by the
            PageRank Markov chain transition matrix!
            """);
    }

    private static void runWithDampingFactor(PrintStream out, double alpha, String title) {
        out.println(TableFormatter.subHeader(title));

        int n = PAGES.length;
        double[][] matrix = buildPageRankMatrix(alpha, n);
        MarkovChain chain = new MarkovChain(
            "PageRank (α=" + alpha + ")", "",
            List.of(PAGES), TransitionMatrix.of(matrix)
        );

        double[] pageRank = MarkovAnalysis.stationaryDistribution(chain);

        // Sort by PageRank (descending)
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Double.compare(pageRank[b], pageRank[a]));

        out.printf("  %-6s  %-12s  %-12s  %s%n", "Rank", "Page", "PageRank", "Bar");
        out.println("  " + "-".repeat(65));
        for (int rank = 0; rank < n; rank++) {
            int i = order[rank];
            int barLen = (int) Math.round(pageRank[i] * 300);
            out.printf("  #%-5d  %-12s  %-12.6f  %s%n",
                rank + 1, PAGES[i], pageRank[i], "█".repeat(barLen));
        }
        out.println();
    }

    /**
     * Builds the PageRank transition matrix with teleportation factor α.
     *
     * <p>P[i][j] = α·(1/n) + (1-α)·A[i][j]/outDegree[i]
     * <p>For dangling nodes (no outgoing links), we use uniform teleportation.
     */
    private static double[][] buildPageRankMatrix(double alpha, int n) {
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            // Count outgoing links
            int outDegree = 0;
            for (int j = 0; j < n; j++) outDegree += LINKS[i][j];

            for (int j = 0; j < n; j++) {
                double linkProb = (outDegree > 0)
                    ? (double) LINKS[i][j] / outDegree
                    : 1.0 / n;  // dangling node: uniform
                matrix[i][j] = alpha / n + (1.0 - alpha) * linkProb;
            }
        }
        return matrix;
    }
}
