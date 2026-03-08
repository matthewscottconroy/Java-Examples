package com.examples.dilemma.gui;

import com.examples.dilemma.engine.MatchResult;
import com.examples.dilemma.engine.StrategyLoader;
import com.examples.dilemma.engine.Tournament;
import com.examples.dilemma.engine.TournamentResult;
import com.examples.dilemma.io.CsvExporter;
import com.examples.dilemma.strategy.Strategy;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swing GUI for the Prisoner's Dilemma tournament.
 *
 * <p>The window is divided into three areas:
 * <ul>
 *   <li><strong>Top toolbar</strong> — Run, Export CSV, rounds spinner, sort controls.</li>
 *   <li><strong>Left panel</strong> — strategy selection checkboxes.</li>
 *   <li><strong>Center tabs</strong> — Standings table, Match results, Score chart.</li>
 * </ul>
 *
 * <p>Tournaments run on a background thread ({@link SwingWorker}) so the UI stays responsive.
 * Launch with: {@code mvn exec:java -Dexec.mainClass=com.examples.dilemma.gui.TournamentGui}
 */
public final class TournamentGui extends JFrame {

    // --- column headers ---
    private static final String[] STANDINGS_COLS =
            {"Rank", "Strategy", "Score", "Avg/Round", "Match W", "Match L", "Match D"};
    private static final String[] MATCHES_COLS =
            {"Strategy A", "Score A", "Score B", "Strategy B", "Winner"};

    // --- state ---
    private final List<Strategy> allStrategies = StrategyLoader.loadAll(Path.of("strategies"));
    private final Map<Strategy, JCheckBox> checkBoxes = new LinkedHashMap<>();
    private List<TournamentResult.Standing> currentStandings = new ArrayList<>();
    private TournamentResult lastResult;

    // --- models ---
    private final DefaultTableModel standingsModel = new DefaultTableModel(STANDINGS_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel matchesModel = new DefaultTableModel(MATCHES_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // --- widgets that need cross-method access ---
    private final BarChartPanel chartPanel  = new BarChartPanel();
    private final JLabel        statusLabel = new JLabel("Ready — select strategies and click Run Tournament.");
    private final JSpinner      roundsSpinner =
            new JSpinner(new SpinnerNumberModel(200, 10, 10_000, 50));
    private final JComboBox<String> sortBox =
            new JComboBox<>(new String[]{"Total Score", "Avg per Round", "Match Wins", "Name"});
    private final JButton runBtn    = new JButton("Run Tournament");
    private final JButton exportBtn = new JButton("Export CSV");

    /** Creates and lays out the window. */
    public TournamentGui() {
        super("Prisoner's Dilemma Tournament");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildToolbar(),       BorderLayout.NORTH);
        add(buildStrategyPanel(), BorderLayout.WEST);
        add(buildResultsPanel(),  BorderLayout.CENTER);
        add(buildStatusBar(),     BorderLayout.SOUTH);

        runBtn.addActionListener(e -> runTournament());
        exportBtn.addActionListener(e -> exportCsv());
        sortBox.addActionListener(e -> sortStandings((String) sortBox.getSelectedItem()));
    }

    // -------------------------------------------------------------------------
    // Layout builders
    // -------------------------------------------------------------------------

    private JToolBar buildToolbar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        ((JSpinner.DefaultEditor) roundsSpinner.getEditor()).getTextField().setColumns(5);

        bar.add(runBtn);
        bar.addSeparator();
        bar.add(new JLabel("  Rounds per match: "));
        bar.add(roundsSpinner);
        bar.addSeparator();
        bar.add(new JLabel("  Sort by: "));
        bar.add(sortBox);
        bar.addSeparator();
        bar.add(exportBtn);
        return bar;
    }

    private JPanel buildStrategyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Strategies"));
        panel.setPreferredSize(new Dimension(200, 0));

        for (Strategy s : allStrategies) {
            JCheckBox cb = new JCheckBox(s.getName(), true);
            cb.setToolTipText(s.getDescription());
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            checkBoxes.put(s, cb);
            panel.add(cb);
        }

        panel.add(Box.createVerticalStrut(6));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(4));

        JButton selectAll   = new JButton("All");
        JButton deselectAll = new JButton("None");
        selectAll.addActionListener(e -> checkBoxes.values().forEach(c -> c.setSelected(true)));
        deselectAll.addActionListener(e -> checkBoxes.values().forEach(c -> c.setSelected(false)));

        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        toggleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleRow.add(selectAll);
        toggleRow.add(deselectAll);
        panel.add(toggleRow);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JTabbedPane buildResultsPanel() {
        JTable standingsTable = new JTable(standingsModel);
        standingsTable.setFillsViewportHeight(true);
        standingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTable matchesTable = new JTable(matchesModel);
        matchesTable.setFillsViewportHeight(true);
        matchesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Standings",   new JScrollPane(standingsTable));
        tabs.addTab("Matches",     new JScrollPane(matchesTable));
        tabs.addTab("Score Chart", chartPanel);
        return tabs;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createLoweredBevelBorder());
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    private void runTournament() {
        List<Strategy> selected = new ArrayList<>();
        checkBoxes.forEach((s, cb) -> { if (cb.isSelected()) selected.add(s); });

        if (selected.size() < 2) {
            statusLabel.setText("Select at least 2 strategies to run a tournament.");
            return;
        }

        int rounds = (Integer) roundsSpinner.getValue();
        runBtn.setEnabled(false);
        statusLabel.setText("Running tournament\u2026");

        SwingWorker<TournamentResult, String> worker = new SwingWorker<TournamentResult, String>() {
            @Override
            protected TournamentResult doInBackground() {
                Tournament t = new Tournament(selected, rounds);
                return t.run(msg -> publish(msg));
            }

            @Override
            protected void process(List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    lastResult = get();
                    currentStandings = new ArrayList<>(lastResult.standings());
                    refreshStandingsTable();
                    populateMatchesTable(lastResult);
                    chartPanel.setData(currentStandings);
                    statusLabel.setText("Tournament complete — "
                            + lastResult.matches().size() + " matches played, "
                            + rounds + " rounds each.");
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                } finally {
                    runBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void exportCsv() {
        if (lastResult == null) {
            statusLabel.setText("Run the tournament first.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose export directory");
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            CsvExporter.export(lastResult, chooser.getSelectedFile().toPath());
            statusLabel.setText("Exported CSV files to: "
                    + chooser.getSelectedFile().getAbsolutePath());
        } catch (IOException ex) {
            statusLabel.setText("Export failed: " + ex.getMessage());
        }
    }

    private void sortStandings(String criterion) {
        if (currentStandings.isEmpty()) return;
        switch (criterion) {
            case "Avg per Round" -> currentStandings.sort((a, b) ->
                    Double.compare(b.avgScorePerRound(), a.avgScorePerRound()));
            case "Match Wins"    -> currentStandings.sort((a, b) ->
                    Integer.compare(b.wins(), a.wins()));
            case "Name"          -> currentStandings.sort((a, b) ->
                    a.strategyName().compareTo(b.strategyName()));
            default              -> currentStandings.sort((a, b) ->
                    Integer.compare(b.totalScore(), a.totalScore()));
        }
        refreshStandingsTable();
        chartPanel.setData(currentStandings);
    }

    // -------------------------------------------------------------------------
    // Table population
    // -------------------------------------------------------------------------

    private void refreshStandingsTable() {
        standingsModel.setRowCount(0);
        for (int i = 0; i < currentStandings.size(); i++) {
            TournamentResult.Standing s = currentStandings.get(i);
            standingsModel.addRow(new Object[]{
                i + 1,
                s.strategyName(),
                s.totalScore(),
                String.format("%.4f", s.avgScorePerRound()),
                s.wins(),
                s.losses(),
                s.draws()
            });
        }
    }

    private void populateMatchesTable(TournamentResult result) {
        matchesModel.setRowCount(0);
        for (MatchResult m : result.matches()) {
            matchesModel.addRow(new Object[]{
                m.nameA(), m.scoreA(), m.scoreB(), m.nameB(), m.winner()
            });
        }
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /** Launches the GUI on the Swing event dispatch thread. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TournamentGui().setVisible(true));
    }
}
