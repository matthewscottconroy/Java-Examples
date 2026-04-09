package com.markovmonopoly;

import com.markovmonopoly.core.*;
import com.markovmonopoly.editor.MarkovChainEditor;
import com.markovmonopoly.examples.ExampleRunner;
import com.markovmonopoly.monopoly.board.MonopolyBoard;
import com.markovmonopoly.monopoly.markov.MonopolyMarkovChainBuilder;
import com.markovmonopoly.monopoly.simulation.MonopolySimulator;
import com.markovmonopoly.monopoly.simulation.SimulationStats;
import com.markovmonopoly.ui.ConsoleMenu;
import com.markovmonopoly.ui.TableFormatter;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Entry point for the Markov Monopoly educational application.
 *
 * <h2>Application Overview</h2>
 * <p>This program explores Markov chains — mathematical models of random processes
 * where the future depends only on the present state, not on history. Monopoly
 * provides a rich, familiar example of a Markov chain with 41 states.
 *
 * <h2>Main Menu</h2>
 * <ol>
 *   <li>Classic examples: Weather, Gambler's Ruin, PageRank, Ehrenfest</li>
 *   <li>Markov Chain Editor: design, save, and analyze custom chains</li>
 *   <li>Monopoly Simulator: simulate the game and build the Markov chain from data</li>
 * </ol>
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        Scanner in  = new Scanner(System.in);
        PrintStream out = System.out;

        printWelcome(out);

        ConsoleMenu mainMenu = new ConsoleMenu(
            "Markov Monopoly — Main Menu",
            List.of(
                new ConsoleMenu.MenuItem("1", "Classic Markov Chain Examples"),
                new ConsoleMenu.MenuItem("2", "Markov Chain Editor (design, save, load, analyze)"),
                new ConsoleMenu.MenuItem("3", "Monopoly Simulator"),
                new ConsoleMenu.MenuItem("q", "Quit")
            ),
            in, out
        );

        while (true) {
            String choice = mainMenu.prompt();
            switch (choice) {
                case "1" -> ExampleRunner.run(in, out);
                case "2" -> new MarkovChainEditor(in, out).run();
                case "3" -> runMonopolyMenu(in, out);
                case "q" -> {
                    out.println("\nGoodbye! Remember: the Markov property makes everything tractable.");
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Monopoly submenu
    // -------------------------------------------------------------------------

    private static void runMonopolyMenu(Scanner in, PrintStream out) {
        ConsoleMenu menu = new ConsoleMenu(
            "Monopoly Simulator",
            List.of(
                new ConsoleMenu.MenuItem("1", "Quick simulation — run N turns, show landing frequencies"),
                new ConsoleMenu.MenuItem("2", "Build empirical Markov chain from simulation"),
                new ConsoleMenu.MenuItem("3", "Build theoretical Markov chain (analytical)"),
                new ConsoleMenu.MenuItem("4", "Compare empirical vs. theoretical chain"),
                new ConsoleMenu.MenuItem("5", "Analyze top landing spots with bar chart"),
                new ConsoleMenu.MenuItem("6", "Save Monopoly chain to file"),
                new ConsoleMenu.MenuItem("b", "Back to main menu")
            ),
            in, out
        );

        MonopolyBoard board = MonopolyBoard.standard();
        SimulationStats lastStats = null;

        while (true) {
            String choice = menu.prompt();
            switch (choice) {
                case "1" -> {
                    lastStats = runQuickSimulation(in, out, board);
                }
                case "2" -> {
                    if (lastStats == null) {
                        out.println("  Run a simulation first [1].");
                    } else {
                        buildAndDisplayChain(out, lastStats, board);
                    }
                }
                case "3" -> buildTheoreticalChain(out, board);
                case "4" -> {
                    if (lastStats == null) {
                        out.println("  Run a simulation first [1].");
                    } else {
                        compareChains(out, lastStats, board);
                    }
                }
                case "5" -> {
                    if (lastStats == null) {
                        out.println("  Run a simulation first [1].");
                    } else {
                        showLandingChart(out, lastStats, board);
                    }
                }
                case "6" -> {
                    if (lastStats == null) {
                        out.println("  Run a simulation first [1].");
                    } else {
                        saveMonopolyChain(in, out, lastStats, board);
                    }
                }
                case "b" -> { return; }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Monopoly operations
    // -------------------------------------------------------------------------

    private static SimulationStats runQuickSimulation(Scanner in, PrintStream out,
                                                       MonopolyBoard board) {
        out.println(TableFormatter.sectionHeader("MONOPOLY QUICK SIMULATION"));
        out.println("""
            We simulate a single player taking many turns around the Monopoly board.
            Each simulated turn records all position transitions.
            After enough turns, the empirical frequencies converge to the stationary
            distribution of the Monopoly Markov chain.
            """);

        ConsoleMenu prompt = new ConsoleMenu("", List.of(), in, out);
        int turns = prompt.promptInt("Number of turns to simulate [1000–1000000]: ", 1_000, 1_000_000);

        out.printf("%n  Simulating %,d turns...%n", turns);
        long start = System.currentTimeMillis();

        MonopolySimulator simulator = new MonopolySimulator();
        SimulationStats stats = simulator.simulateAndCollect(turns);

        long elapsed = System.currentTimeMillis() - start;
        out.printf("  Done in %d ms. Recorded %,d transitions.%n%n", elapsed,
            stats.getTotalTransitions());

        // Show top 10 most visited
        out.println(TableFormatter.subHeader("TOP 15 MOST VISITED SPACES"));
        List<String> labels = MonopolyMarkovChainBuilder.buildLabels(board);
        showTopSpaces(out, stats, labels, 15);

        out.println();
        out.println("  Full frequency table available in [5]. Markov chain in [2].");
        return stats;
    }

    private static void buildAndDisplayChain(PrintStream out, SimulationStats stats,
                                              MonopolyBoard board) {
        out.println(TableFormatter.sectionHeader("EMPIRICAL MONOPOLY MARKOV CHAIN"));
        out.println("""
            The empirical Markov chain is built by dividing the transition counts by
            row totals: P[i][j] = count(i→j) / count(i→*).

            As the number of simulated turns increases, this empirical chain converges
            to the true theoretical chain (Law of Large Numbers).
            """);

        MarkovChain chain = MonopolyMarkovChainBuilder.fromSimulation(stats, board);
        List<String> labels = chain.getStateLabels();

        out.printf("  Chain: %s%n", chain.getName());
        out.printf("  States: %d  |  Transitions recorded: %,d%n%n",
            chain.size(), stats.getTotalTransitions());

        // Stationary distribution
        out.println(TableFormatter.subHeader("Stationary Distribution (Visit Frequencies)"));
        double[] pi = MarkovAnalysis.stationaryDistribution(chain);
        out.print(TableFormatter.asciiBarChart(pi, labels, 35));

        // State classification
        out.println(TableFormatter.subHeader("Chain Properties"));
        out.printf("  Irreducible: %b  |  Ergodic: %b%n",
            MarkovAnalysis.isIrreducible(chain), MarkovAnalysis.isErgodic(chain));
        out.println();
        out.println("  (All states are reachable — the chain is irreducible and ergodic.)");
    }

    private static void buildTheoreticalChain(PrintStream out, MonopolyBoard board) {
        out.println(TableFormatter.sectionHeader("THEORETICAL MONOPOLY MARKOV CHAIN"));
        out.println("""
            The theoretical chain computes exact transition probabilities by:
            1. Enumerating all 36 equally-likely dice outcomes (d1,d2 for d1,d2 ∈ 1..6)
            2. Applying card effects with uniform probability (1/16 per card)
            3. Handling the Go To Jail space (always transitions to state 40)
            4. Modeling jail exit probabilities (mixture over 3 jail turns)

            Note: This 41-state model approximates jail mechanics. The exact model
            needs 43 states (JAIL_1, JAIL_2, JAIL_3). Jail approximation is standard
            in academic treatments (Stewart 2009, Abbott 2014).
            """);

        out.println("  Computing theoretical chain...");
        MarkovChain chain = MonopolyMarkovChainBuilder.buildTheoretical(board);
        out.println("  Done.\n");

        List<String> labels = chain.getStateLabels();
        double[] pi = MarkovAnalysis.stationaryDistribution(chain);

        out.println(TableFormatter.subHeader("Stationary Distribution — Most Visited Spaces"));
        out.print(TableFormatter.asciiBarChart(pi, labels, 35));

        // Top 10 by stationary distribution
        Integer[] order = sortByValue(pi);
        out.println(TableFormatter.subHeader("Top 10 Most Visited (Theoretical)"));
        out.printf("  %-4s  %-26s  %-12s%n", "Rank", "Space", "Long-run %");
        out.println("  " + "-".repeat(46));
        for (int rank = 0; rank < 10; rank++) {
            int i = order[rank];
            out.printf("  #%-3d  %-26s  %.3f%%%n",
                rank + 1, labels.get(i), pi[i] * 100);
        }
        out.println();
        out.println("""
            KEY RESULT: Jail (state 40) is the most visited single state because:
            - Three doubles in a row sends you there
            - Landing on space 30 sends you there
            - Two Chance cards and one Community Chest card send you there

            Illinois Ave (24) and B&O Railroad (25) rank highly because Jail releases
            players who then land near those spaces via typical dice totals (6-9).
            """);
    }

    private static void compareChains(PrintStream out, SimulationStats stats,
                                       MonopolyBoard board) {
        out.println(TableFormatter.sectionHeader("EMPIRICAL vs. THEORETICAL COMPARISON"));
        out.println("""
            Comparing the simulation-derived chain against the theoretical chain
            shows how well the Monte Carlo method works as an approximation.
            The L∞ norm measures the largest difference in any transition probability.
            """);

        MarkovChain empirical    = MonopolyMarkovChainBuilder.fromSimulation(stats, board);
        MarkovChain theoretical  = MonopolyMarkovChainBuilder.buildTheoretical(board);
        List<String> labels      = MonopolyMarkovChainBuilder.buildLabels(board);

        double[] piEmp   = MarkovAnalysis.stationaryDistribution(empirical);
        double[] piTheor = MarkovAnalysis.stationaryDistribution(theoretical);
        double tv        = MarkovAnalysis.totalVariationDistance(piEmp, piTheor);

        out.printf("  Simulated turns:           %,d%n", stats.getTotalTransitions());
        out.printf("  TV distance (stationary):  %.6f%n", tv);
        out.printf("  (TV=0 is perfect; TV=1 is completely different)%n%n");

        out.println(TableFormatter.subHeader("Per-State Comparison (sorted by theoretical probability)"));
        Integer[] order = sortByValue(piTheor);
        out.printf("  %-26s  %-12s  %-12s  %-10s%n", "Space", "Theoretical", "Empirical", "Diff");
        out.println("  " + "-".repeat(64));
        for (int idx : order) {
            double diff = piEmp[idx] - piTheor[idx];
            out.printf("  %-26s  %-12.4f  %-12.4f  %+.4f%n",
                truncate(labels.get(idx), 26), piTheor[idx], piEmp[idx], diff);
        }
    }

    private static void showLandingChart(PrintStream out, SimulationStats stats,
                                          MonopolyBoard board) {
        out.println(TableFormatter.sectionHeader("LANDING FREQUENCY CHART"));
        List<String> labels = MonopolyMarkovChainBuilder.buildLabels(board);
        out.print(stats.toFrequencyTable(labels));
    }

    private static void saveMonopolyChain(Scanner in, PrintStream out,
                                           SimulationStats stats, MonopolyBoard board) {
        ConsoleMenu prompt = new ConsoleMenu("", List.of(), in, out);
        String filename = prompt.promptString("Save to file: ");
        MarkovChain chain = MonopolyMarkovChainBuilder.fromSimulation(stats, board);
        try {
            MarkovChainIO.save(chain, Path.of(filename));
            out.println("  Saved to " + Path.of(filename).toAbsolutePath());
        } catch (IOException e) {
            out.println("  Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------------------------

    private static void showTopSpaces(PrintStream out, SimulationStats stats,
                                       List<String> labels, int topN) {
        long[] counts = stats.getLandingCounts();
        long total = stats.getTotalTransitions();
        Integer[] order = new Integer[counts.length];
        for (int i = 0; i < counts.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Long.compare(counts[b], counts[a]));

        out.printf("  %-4s  %-26s  %-10s  %-8s%n", "Rank", "Space", "Landings", "Freq %");
        out.println("  " + "-".repeat(54));
        for (int rank = 0; rank < Math.min(topN, order.length); rank++) {
            int i = order[rank];
            double pct = total > 0 ? 100.0 * counts[i] / total : 0;
            out.printf("  #%-3d  %-26s  %-10d  %.2f%%%n",
                rank + 1, truncate(labels.get(i), 26), counts[i], pct);
        }
    }

    private static Integer[] sortByValue(double[] values) {
        Integer[] order = new Integer[values.length];
        for (int i = 0; i < values.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Double.compare(values[b], values[a]));
        return order;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // -------------------------------------------------------------------------
    // Welcome banner
    // -------------------------------------------------------------------------

    private static void printWelcome(PrintStream out) {
        out.println("""
            ╔══════════════════════════════════════════════════════════════════╗
            ║              MARKOV MONOPOLY  —  Educational Edition            ║
            ║                                                                  ║
            ║  Explore Markov chains through the lens of Monopoly:            ║
            ║    • Classic examples (Weather, PageRank, Gambler's Ruin, ...)  ║
            ║    • Interactive chain editor (design, analyze, save chains)    ║
            ║    • Monopoly simulator (build the chain from real game data)   ║
            ║                                                                  ║
            ║  A Markov chain is a random process with the Markov property:   ║
            ║  the future depends only on the PRESENT state, not the past.    ║
            ╚══════════════════════════════════════════════════════════════════╝
            """);
    }
}
