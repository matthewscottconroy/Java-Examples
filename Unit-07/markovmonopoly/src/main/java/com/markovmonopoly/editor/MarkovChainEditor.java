package com.markovmonopoly.editor;

import com.markovmonopoly.core.*;
import com.markovmonopoly.examples.GamblersRuinExample;
import com.markovmonopoly.ui.ConsoleMenu;
import com.markovmonopoly.ui.TableFormatter;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.*;

/**
 * Interactive console editor for creating, inspecting, and saving Markov chains.
 *
 * <h2>Workflow</h2>
 * <ol>
 *   <li>Define states (add state names)</li>
 *   <li>Set transition probabilities (from state → to state: probability)</li>
 *   <li>Validate that each row sums to 1</li>
 *   <li>Analyze: stationary distribution, MFPT, state classification</li>
 *   <li>Save to a text file for later use</li>
 * </ol>
 *
 * <p>The editor uses a mutable double[][] scratchpad and converts to an
 * immutable {@link MarkovChain} only when saving or analyzing.
 */
public final class MarkovChainEditor {

    private final Scanner in;
    private final PrintStream out;
    private final ConsoleMenu mainMenu;

    // Mutable working state
    private String chainName = "Unnamed";
    private String chainDescription = "";
    private List<String> labels = new ArrayList<>();
    private double[][] matrix = new double[0][0];
    private boolean modified = false;

    public MarkovChainEditor(Scanner in, PrintStream out) {
        this.in  = in;
        this.out = out;
        this.mainMenu = new ConsoleMenu(
            "Markov Chain Editor",
            List.of(
                new ConsoleMenu.MenuItem("n", "New chain — start from scratch"),
                new ConsoleMenu.MenuItem("a", "Add a state"),
                new ConsoleMenu.MenuItem("r", "Remove a state"),
                new ConsoleMenu.MenuItem("s", "Set a transition probability"),
                new ConsoleMenu.MenuItem("w", "Set entire row (all transitions from one state)"),
                new ConsoleMenu.MenuItem("v", "View the current matrix"),
                new ConsoleMenu.MenuItem("c", "Check / validate (rows must sum to 1)"),
                new ConsoleMenu.MenuItem("z", "Normalize all rows to sum to 1"),
                new ConsoleMenu.MenuItem("x", "Analyze the current chain"),
                new ConsoleMenu.MenuItem("l", "Load a chain from file"),
                new ConsoleMenu.MenuItem("f", "Save chain to file"),
                new ConsoleMenu.MenuItem("e", "Examples: load a built-in example chain"),
                new ConsoleMenu.MenuItem("b", "Back to main menu")
            ),
            in, out
        );
    }

    public void run() {
        while (true) {
            String choice = mainMenu.prompt();
            switch (choice) {
                case "n" -> cmdNew();
                case "a" -> cmdAddState();
                case "r" -> cmdRemoveState();
                case "s" -> cmdSetTransition();
                case "w" -> cmdSetRow();
                case "v" -> cmdView();
                case "c" -> cmdValidate();
                case "z" -> cmdNormalize();
                case "x" -> cmdAnalyze();
                case "l" -> cmdLoad();
                case "f" -> cmdSave();
                case "e" -> cmdLoadExample();
                case "b" -> { if (confirmDiscard()) return; }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    private void cmdNew() {
        if (!confirmDiscard()) return;
        chainName = mainMenu.promptString("Chain name: ");
        chainDescription = mainMenu.promptOptionalString("Description (optional): ");
        labels.clear();
        matrix = new double[0][0];
        modified = false;
        out.println("  New chain '" + chainName + "' created. Add states with [a].");
    }

    private void cmdAddState() {
        String label = mainMenu.promptString("State name: ");
        if (labels.contains(label)) {
            out.println("  State '" + label + "' already exists.");
            return;
        }
        int n = labels.size() + 1;
        double[][] newMatrix = new double[n][n];
        for (int i = 0; i < n - 1; i++) {
            System.arraycopy(matrix[i], 0, newMatrix[i], 0, n - 1);
        }
        labels.add(label);
        matrix = newMatrix;
        modified = true;
        out.printf("  State '%s' added. Chain now has %d states.%n", label, n);
        out.println("  Set transitions from/to this state with [s] or [w].");
    }

    private void cmdRemoveState() {
        if (labels.isEmpty()) { out.println("  No states to remove."); return; }
        showStateList();
        int idx = mainMenu.promptInt("Remove state index (0-based): ", 0, labels.size() - 1);
        String removed = labels.remove(idx);
        int n = labels.size();
        double[][] newMatrix = new double[n][n];
        for (int i = 0, ni = 0; i < n + 1; i++) {
            if (i == idx) continue;
            for (int j = 0, nj = 0; j < n + 1; j++) {
                if (j == idx) continue;
                newMatrix[ni][nj++] = matrix[i][j];
            }
            ni++;
        }
        matrix = newMatrix;
        modified = true;
        out.printf("  State '%s' removed.%n", removed);
    }

    private void cmdSetTransition() {
        if (labels.size() < 2) { out.println("  Need at least 2 states."); return; }
        showStateList();
        int from = mainMenu.promptInt("From state (index): ", 0, labels.size() - 1);
        int to   = mainMenu.promptInt("To state (index):   ", 0, labels.size() - 1);
        double p = mainMenu.promptDouble(
            String.format("P(%s → %s) [0.0–1.0]: ", labels.get(from), labels.get(to)),
            0.0, 1.0);
        matrix[from][to] = p;
        modified = true;
        out.printf("  Set P(%s → %s) = %.6f%n", labels.get(from), labels.get(to), p);

        double rowSum = rowSum(from);
        if (Math.abs(rowSum - 1.0) > 0.001) {
            out.printf("  Note: row '%s' now sums to %.6f (should be 1.0). Use [z] to normalize.%n",
                labels.get(from), rowSum);
        }
    }

    private void cmdSetRow() {
        if (labels.isEmpty()) { out.println("  No states yet."); return; }
        showStateList();
        int from = mainMenu.promptInt("Set row for state (index): ", 0, labels.size() - 1);
        out.println("  Enter transition probabilities from '" + labels.get(from) +
                    "' to each other state.");
        out.println("  Values must sum to 1.0 (type 'auto' for the last state to fill automatically).");
        out.println();

        double[] row = new double[labels.size()];
        double remaining = 1.0;
        for (int j = 0; j < labels.size(); j++) {
            boolean isLast = (j == labels.size() - 1);
            if (isLast && remaining >= 0 && remaining <= 1.0) {
                out.printf("  P(→ %-12s) [auto = %.6f]: ", labels.get(j), remaining);
                String input = in.nextLine().trim();
                if (input.equalsIgnoreCase("auto") || input.isEmpty()) {
                    row[j] = remaining;
                    out.printf("    Set to %.6f%n", remaining);
                } else {
                    try { row[j] = Double.parseDouble(input); } catch (NumberFormatException e) { row[j] = remaining; }
                }
            } else {
                row[j] = mainMenu.promptDouble(
                    String.format("  P(→ %-12s): ", labels.get(j)), 0.0, 1.0);
                remaining -= row[j];
            }
        }
        matrix[from] = row;
        modified = true;
        out.printf("  Row '%s' set. Sum = %.6f%n", labels.get(from), rowSum(from));
    }

    private void cmdView() {
        if (labels.isEmpty()) { out.println("  Chain is empty. Add states with [a]."); return; }
        out.println();
        out.println("  Chain: " + chainName);
        if (!chainDescription.isEmpty()) out.println("  " + chainDescription);
        out.println("  States: " + labels.size());
        out.println();
        out.print(TableFormatter.formatMatrix(matrix, labels));
        out.println();
        printRowSums();
    }

    private void cmdValidate() {
        if (labels.isEmpty()) { out.println("  Chain is empty."); return; }
        out.println();
        boolean valid = true;
        for (int i = 0; i < labels.size(); i++) {
            double sum = rowSum(i);
            boolean ok = Math.abs(sum - 1.0) < TransitionMatrix.ROW_SUM_TOLERANCE;
            out.printf("  Row %-14s sum = %.6f  %s%n",
                "'" + labels.get(i) + "'", sum, ok ? "✓" : "✗ INVALID (should be 1.0)");
            if (!ok) valid = false;
        }
        out.println();
        out.println(valid ? "  ✓ Chain is valid." : "  ✗ Chain is invalid. Use [z] to normalize rows.");
    }

    private void cmdNormalize() {
        if (labels.isEmpty()) { out.println("  Chain is empty."); return; }
        for (int i = 0; i < labels.size(); i++) {
            double sum = rowSum(i);
            if (sum > 0) {
                for (int j = 0; j < labels.size(); j++) matrix[i][j] /= sum;
            } else {
                Arrays.fill(matrix[i], 1.0 / labels.size());  // uniform fallback
            }
        }
        modified = true;
        out.println("  All rows normalized to sum to 1.0.");
        cmdValidate();
    }

    private void cmdAnalyze() {
        if (labels.size() < 2) { out.println("  Need at least 2 states to analyze."); return; }

        MarkovChain chain = buildChain();
        if (chain == null) {
            out.println("  Chain has invalid rows. Run [z] to normalize first.");
            return;
        }

        out.println();
        out.println(TableFormatter.sectionHeader("ANALYSIS: " + chainName));

        // State classification
        out.println(TableFormatter.subHeader("State Classification"));
        Map<Integer, StateClass> classes = MarkovAnalysis.classifyAllStates(chain);
        for (int i = 0; i < chain.size(); i++) {
            out.printf("  %-16s → %s%n", labels.get(i), classes.get(i));
        }
        out.println();
        out.printf("  Irreducible: %b  |  Aperiodic: %b  |  Ergodic: %b%n",
            MarkovAnalysis.isIrreducible(chain),
            MarkovAnalysis.isAperiodic(chain),
            MarkovAnalysis.isErgodic(chain));
        out.println();

        // Communication classes
        List<List<Integer>> sccs = MarkovAnalysis.communicationClasses(chain);
        out.println(TableFormatter.subHeader("Communication Classes"));
        for (int c = 0; c < sccs.size(); c++) {
            List<Integer> scc = sccs.get(c);
            StringBuilder sb = new StringBuilder();
            for (int idx : scc) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(labels.get(idx));
            }
            out.printf("  Class %d: {%s}%n", c + 1, sb);
        }
        out.println();

        // Stationary distribution (only for ergodic or attempt anyway)
        out.println(TableFormatter.subHeader("Stationary Distribution"));
        double[] pi = MarkovAnalysis.stationaryDistribution(chain);
        out.print(TableFormatter.formatDistribution(pi, labels));

        // Mean first passage times
        if (chain.size() <= 15) {
            out.println(TableFormatter.subHeader("Mean First Passage Times (steps)"));
            double[][] mfpt = MarkovAnalysis.meanFirstPassageTimes(chain);
            out.print(TableFormatter.formatMatrix(mfpt, labels));
        } else {
            out.println("  (MFPT table omitted for chains with > 15 states — too large to display)");
        }

        // Periodicity
        out.println(TableFormatter.subHeader("Periodicity"));
        for (int i = 0; i < chain.size(); i++) {
            int period = MarkovAnalysis.period(chain, i);
            out.printf("  Period of %-14s = %d%n", "'" + labels.get(i) + "'", period);
        }
        out.println();
    }

    private void cmdSave() {
        if (labels.isEmpty()) { out.println("  Nothing to save."); return; }
        MarkovChain chain = buildChain();
        if (chain == null) {
            if (!mainMenu.promptYesNo("  Chain has invalid rows. Save anyway?")) return;
            chain = new MarkovChain(chainName, chainDescription, labels,
                TransitionMatrix.of(matrix).normalized());
        }
        String filename = mainMenu.promptString("File path (e.g., chains/weather.mc): ");
        try {
            Path path = Path.of(filename);
            MarkovChainIO.save(chain, path);
            out.println("  Saved to " + path.toAbsolutePath());
            modified = false;
        } catch (IOException e) {
            out.println("  Error saving: " + e.getMessage());
        }
    }

    private void cmdLoad() {
        if (!confirmDiscard()) return;
        String filename = mainMenu.promptString("File path: ");
        try {
            MarkovChain chain = MarkovChainIO.load(Path.of(filename));
            loadFromChain(chain);
            out.println("  Loaded '" + chainName + "' with " + labels.size() + " states.");
        } catch (IOException e) {
            out.println("  Error loading: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            out.println("  Format error: " + e.getMessage());
        }
    }

    private void cmdLoadExample() {
        ConsoleMenu examples = new ConsoleMenu(
            "Built-in Example Chains",
            List.of(
                new ConsoleMenu.MenuItem("1", "Weather Model (3 states)"),
                new ConsoleMenu.MenuItem("2", "Gambler's Ruin (5 states, N=4, p=0.5)"),
                new ConsoleMenu.MenuItem("3", "Two-State Chain (symmetric)"),
                new ConsoleMenu.MenuItem("4", "Absorbing chain with 2 transient states"),
                new ConsoleMenu.MenuItem("b", "Back")
            ),
            in, out
        );

        String choice = examples.prompt();
        MarkovChain chain = switch (choice) {
            case "1" -> MarkovChain.of("Weather Model", "Classic 3-state chain",
                new String[]{"Sunny", "Cloudy", "Rainy"},
                new double[][]{{0.7, 0.2, 0.1}, {0.3, 0.4, 0.3}, {0.2, 0.3, 0.5}});
            case "2" -> GamblersRuinExample.buildGamblersRuin(4, 0.5);
            case "3" -> MarkovChain.of("Two-State Chain", "",
                new String[]{"A", "B"},
                new double[][]{{0.6, 0.4}, {0.3, 0.7}});
            case "4" -> MarkovChain.of("Absorbing Chain", "",
                new String[]{"Start", "Middle", "Win", "Lose"},
                new double[][]{{0.0, 0.7, 0.2, 0.1},
                               {0.0, 0.0, 0.4, 0.6},
                               {0.0, 0.0, 1.0, 0.0},
                               {0.0, 0.0, 0.0, 1.0}});
            default -> null;
        };

        if (chain != null) {
            loadFromChain(chain);
            out.println("  Loaded '" + chainName + "' with " + labels.size() + " states.");
            cmdView();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void loadFromChain(MarkovChain chain) {
        chainName = chain.getName();
        chainDescription = chain.getDescription();
        labels = new ArrayList<>(chain.getStateLabels());
        matrix = chain.getMatrix().toArray();
        modified = false;
    }

    private MarkovChain buildChain() {
        try {
            TransitionMatrix tm = TransitionMatrix.of(matrix);
            tm.validate();
            return new MarkovChain(chainName, chainDescription, labels, tm);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private void showStateList() {
        out.println("  States:");
        for (int i = 0; i < labels.size(); i++) {
            out.printf("    [%d] %s%n", i, labels.get(i));
        }
    }

    private void printRowSums() {
        for (int i = 0; i < labels.size(); i++) {
            double sum = rowSum(i);
            out.printf("  Row %-14s sum = %.6f%n", "'" + labels.get(i) + "'", sum);
        }
    }

    private double rowSum(int i) {
        double sum = 0;
        for (double v : matrix[i]) sum += v;
        return sum;
    }

    private boolean confirmDiscard() {
        if (!modified || labels.isEmpty()) return true;
        return mainMenu.promptYesNo("  Discard unsaved changes to '" + chainName + "'?");
    }
}
