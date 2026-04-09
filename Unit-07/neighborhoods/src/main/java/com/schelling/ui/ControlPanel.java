package com.schelling.ui;

import com.schelling.model.InitialCondition;
import com.schelling.model.NeighborhoodType;
import com.schelling.model.SimulationConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Sidebar control panel organised into three tabs:
 *
 * <ol>
 *   <li><b>Run</b> — Step / Run / Reset, speed, paint mode, view options</li>
 *   <li><b>Configure</b> — Grid size, per-group thresholds (live), empty
 *       fraction, seed, neighborhood type, initial-condition presets</li>
 *   <li><b>Analyze</b> — Live statistics, event log, export button</li>
 * </ol>
 */
public final class ControlPanel extends JPanel {

    // ── Dark theme ────────────────────────────────────────────────────────────
    private static final Color BG       = new Color(18, 20, 32);
    private static final Color BG_TAB   = new Color(22, 25, 38);
    private static final Color FG       = new Color(190, 205, 230);
    private static final Color FG_DIM   = new Color(100, 115, 145);
    private static final Color ACCENT   = new Color(80, 160, 255);
    private static final Color SEP      = new Color(38, 48, 72);
    private static final Font  MONO     = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    private static final Font  SMALL    = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static final Font  BOLD_SM  = new Font(Font.SANS_SERIF, Font.BOLD,  10);

    // ── Listener interface ────────────────────────────────────────────────────

    public interface SimulationListener {
        void onStep();
        void onRunToggled(boolean running);
        void onReset(SimulationConfig newConfig);
        default void onLiveThresholdChange(double tA, double tB) {}
        default void onViewModeChange(boolean heatmap, boolean highlight) {}
        default void onPaletteChange(GridPanel.ColorPalette palette) {}
        default void onPaintModeChange(GridPanel.PaintMode mode) {}
        default void onExportPng() {}
        default void onRunSweep() {}
        default void onExportCsv() {}
    }

    // ── Run tab controls ──────────────────────────────────────────────────────
    private final JButton       stepButton;
    private final JButton       runButton;
    private final JButton       resetButton;
    private final JSlider       speedSlider;
    private final ButtonGroup   paintGroup;
    private final JToggleButton paintOffBtn;
    private final JToggleButton paintABtn;
    private final JToggleButton paintBBtn;
    private final JToggleButton paintEraseBtn;
    private final JCheckBox     heatmapCb;
    private final JCheckBox     highlightCb;
    private final JCheckBox     colorblindCb;

    // ── Configure tab controls ────────────────────────────────────────────────
    private final JTextField rowsField;
    private final JTextField colsField;
    private final JSlider    threshASlider;
    private final JSlider    threshBSlider;
    private final JLabel     threshALabel;
    private final JLabel     threshBLabel;
    private final JSlider    emptySlider;
    private final JLabel     emptyLabel;
    private final JTextField seedField;
    private final JComboBox<NeighborhoodType> neighborhoodCombo;

    // ── Analyze tab controls ──────────────────────────────────────────────────
    private final JLabel   stepLabel;
    private final JLabel   satLabel;
    private final JLabel   isoALabel;
    private final JLabel   isoBLabel;
    private final JLabel   dissimLabel;
    private final JLabel   movesLabel;
    private final JLabel   statusLabel;
    private final JTextArea eventLog;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean            running  = false;
    private final SimulationListener listener;

    // ── Constructor ───────────────────────────────────────────────────────────

    public ControlPanel(SimulationListener listener) {
        if (listener == null) throw new NullPointerException("listener must not be null");
        this.listener = listener;

        setBackground(BG);
        setPreferredSize(new Dimension(250, 0));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, SEP));

        // ── Build Run tab ────────────────────────────────────────────────────
        stepButton   = styledButton("Step",   new Color(50, 110, 180));
        runButton    = styledButton("▶  Run", new Color(45, 130, 70));
        resetButton  = styledButton("Reset",  new Color(150, 55, 55));

        speedSlider  = new JSlider(50, 2000, 400);
        speedSlider.setInverted(true);
        speedSlider.setBackground(BG_TAB);

        paintOffBtn   = paintToggle("Off");
        paintABtn     = paintToggle("Paint A");
        paintBBtn     = paintToggle("Paint B");
        paintEraseBtn = paintToggle("Erase");
        paintGroup    = new ButtonGroup();
        paintGroup.add(paintOffBtn); paintGroup.add(paintABtn);
        paintGroup.add(paintBBtn);   paintGroup.add(paintEraseBtn);
        paintOffBtn.setSelected(true);

        heatmapCb   = styledCheckbox("Heatmap (color by satisfaction)");
        highlightCb = styledCheckbox("Highlight unsatisfied cells");
        colorblindCb = styledCheckbox("Colorblind palette (blue/orange)");

        JPanel runTab = buildRunTab();

        // ── Build Configure tab ──────────────────────────────────────────────
        rowsField   = new JTextField(String.valueOf(SimulationConfig.DEFAULT_ROWS), 5);
        colsField   = new JTextField(String.valueOf(SimulationConfig.DEFAULT_COLS), 5);
        styleTextField(rowsField);
        styleTextField(colsField);

        threshASlider = percentSlider((int)(SimulationConfig.DEFAULT_THRESHOLD_A * 100));
        threshBSlider = percentSlider((int)(SimulationConfig.DEFAULT_THRESHOLD_B * 100));
        threshALabel  = valueLabel(threshASlider, "%");
        threshBLabel  = valueLabel(threshBSlider, "%");

        emptySlider = percentSlider((int)(SimulationConfig.DEFAULT_EMPTY_FRACTION * 100));
        emptyLabel  = valueLabel(emptySlider, "%");

        seedField = new JTextField(String.valueOf(SimulationConfig.DEFAULT_RANDOM_SEED), 8);
        styleTextField(seedField);

        neighborhoodCombo = new JComboBox<>(NeighborhoodType.values());
        neighborhoodCombo.setBackground(new Color(28, 32, 50));
        neighborhoodCombo.setForeground(FG);
        neighborhoodCombo.setFont(SMALL);

        JPanel configTab = buildConfigTab();

        // ── Build Analyze tab ────────────────────────────────────────────────
        stepLabel   = statLabel("Step: 0");
        satLabel    = statLabel("Satisfaction: —");
        isoALabel   = statLabel("Isolation A:  —");
        isoBLabel   = statLabel("Isolation B:  —");
        dissimLabel = statLabel("Dissimilarity: —");
        movesLabel  = statLabel("Last moves:   —");
        statusLabel = new JLabel("Status: running");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        statusLabel.setForeground(FG_DIM);

        eventLog = new JTextArea(8, 18);
        eventLog.setEditable(false);
        eventLog.setBackground(new Color(12, 14, 22));
        eventLog.setForeground(new Color(160, 180, 220));
        eventLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
        eventLog.setLineWrap(true);

        JPanel analyzeTab = buildAnalyzeTab();

        // ── Tabbed pane ──────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(FG);
        tabs.setFont(BOLD_SM);
        tabs.add("Run",       wrapScroll(runTab));
        tabs.add("Configure", wrapScroll(configTab));
        tabs.add("Analyze",   wrapScroll(analyzeTab));
        add(tabs, BorderLayout.CENTER);

        // ── Wire actions ─────────────────────────────────────────────────────
        wireActions();
    }

    // ── Tab builders ──────────────────────────────────────────────────────────

    private JPanel buildRunTab() {
        JPanel p = darkBox();

        JLabel ctl = sectionLabel("CONTROLS");
        p.add(ctl);
        p.add(strut(3));
        p.add(fillX(stepButton));
        p.add(strut(4));
        p.add(fillX(runButton));
        p.add(strut(4));
        p.add(fillX(resetButton));
        p.add(strut(8));
        p.add(dimLabel("Speed:  slow ◀──▶ fast"));
        p.add(strut(2));
        p.add(fillX(speedSlider));

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("PAINT MODE"));
        p.add(strut(3));
        JPanel paintRow1 = rowPanel();
        paintRow1.add(paintOffBtn); paintRow1.add(paintABtn);
        p.add(paintRow1);
        p.add(strut(2));
        JPanel paintRow2 = rowPanel();
        paintRow2.add(paintBBtn); paintRow2.add(paintEraseBtn);
        p.add(paintRow2);

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("VIEW"));
        p.add(strut(3));
        p.add(heatmapCb);
        p.add(strut(2));
        p.add(highlightCb);
        p.add(strut(2));
        p.add(colorblindCb);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildConfigTab() {
        JPanel p = darkBox();

        p.add(sectionLabel("GRID SIZE"));
        p.add(strut(3));
        JPanel sizeRow = rowPanel();
        sizeRow.add(dimLabel("Rows:"));
        sizeRow.add(rowsField);
        sizeRow.add(Box.createHorizontalStrut(8));
        sizeRow.add(dimLabel("Cols:"));
        sizeRow.add(colsField);
        p.add(sizeRow);

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("THRESHOLDS  (live — no reset needed)"));
        p.add(strut(3));
        p.add(dimLabel("Group A  (blue):"));
        p.add(strut(1));
        p.add(fillX(threshASlider));
        p.add(threshALabel);
        p.add(strut(5));
        p.add(dimLabel("Group B  (red/orange):"));
        p.add(strut(1));
        p.add(fillX(threshBSlider));
        p.add(threshBLabel);

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("EMPTY FRACTION  (apply on Reset)"));
        p.add(strut(3));
        p.add(fillX(emptySlider));
        p.add(emptyLabel);

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("NEIGHBORHOOD TYPE  (apply on Reset)"));
        p.add(strut(3));
        p.add(fillX(neighborhoodCombo));

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("RANDOM SEED  (apply on Reset)"));
        p.add(strut(3));
        JPanel seedRow = rowPanel();
        seedRow.add(fillX(seedField));
        JButton rndSeed = miniButton("⟳");
        rndSeed.setToolTipText("Random seed");
        rndSeed.addActionListener(e ->
            seedField.setText(String.valueOf(System.currentTimeMillis() % 100000)));
        seedRow.add(rndSeed);
        p.add(seedRow);

        p.add(strut(12));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("INITIAL CONDITION"));
        p.add(strut(4));
        for (InitialCondition ic : InitialCondition.values()) {
            JButton btn = styledButton(ic.getDisplayName(), new Color(40, 55, 90));
            btn.addActionListener(e -> listener.onReset(buildConfig(ic)));
            p.add(fillX(btn));
            p.add(strut(3));
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildAnalyzeTab() {
        JPanel p = darkBox();

        p.add(sectionLabel("STATISTICS"));
        p.add(strut(4));
        for (JLabel l : new JLabel[]{stepLabel, satLabel, isoALabel,
                                      isoBLabel, dissimLabel, movesLabel}) {
            p.add(l); p.add(strut(2));
        }
        p.add(strut(3));
        p.add(statusLabel);

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("EVENT LOG"));
        p.add(strut(3));
        JScrollPane logScroll = new JScrollPane(eventLog);
        logScroll.setBackground(new Color(12, 14, 22));
        logScroll.setBorder(BorderFactory.createLineBorder(SEP));
        logScroll.setPreferredSize(new Dimension(220, 140));
        logScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        logScroll.setAlignmentX(LEFT_ALIGNMENT);
        p.add(logScroll);

        p.add(strut(10));
        p.add(separator());
        p.add(strut(6));
        p.add(sectionLabel("EXPORT"));
        p.add(strut(4));
        JButton exportPng = styledButton("Export Grid PNG", new Color(50, 85, 130));
        JButton exportCsv = styledButton("Export History CSV", new Color(50, 85, 130));
        JButton phaseDiag = styledButton("Phase Diagram…", new Color(60, 80, 115));
        exportPng.addActionListener(e -> listener.onExportPng());
        exportCsv.addActionListener(e -> listener.onExportCsv());
        phaseDiag.addActionListener(e -> listener.onRunSweep());
        p.add(fillX(exportPng)); p.add(strut(3));
        p.add(fillX(exportCsv)); p.add(strut(3));
        p.add(fillX(phaseDiag));
        p.add(Box.createVerticalGlue());
        return p;
    }

    // ── Wire actions ──────────────────────────────────────────────────────────

    private void wireActions() {
        stepButton.addActionListener((ActionEvent e) -> {
            if (!running) listener.onStep();
        });

        runButton.addActionListener((ActionEvent e) -> {
            running = !running;
            runButton.setText(running ? "⏸  Pause" : "▶  Run");
            runButton.setBackground(running ? new Color(130, 90, 30) : new Color(45, 130, 70));
            stepButton.setEnabled(!running);
            listener.onRunToggled(running);
        });

        resetButton.addActionListener((ActionEvent e) -> {
            if (running) {
                running = false;
                runButton.setText("▶  Run");
                runButton.setBackground(new Color(45, 130, 70));
                stepButton.setEnabled(true);
                listener.onRunToggled(false);
            }
            listener.onReset(buildConfig(null));
        });

        // Live threshold sliders — fire immediately, no reset required
        threshASlider.addChangeListener(e ->
            listener.onLiveThresholdChange(
                threshASlider.getValue() / 100.0,
                threshBSlider.getValue() / 100.0));
        threshBSlider.addChangeListener(e ->
            listener.onLiveThresholdChange(
                threshASlider.getValue() / 100.0,
                threshBSlider.getValue() / 100.0));

        // View checkboxes
        heatmapCb  .addActionListener(e -> fireViewChange());
        highlightCb.addActionListener(e -> fireViewChange());
        colorblindCb.addActionListener(e -> listener.onPaletteChange(
            colorblindCb.isSelected()
                ? GridPanel.ColorPalette.COLORBLIND
                : GridPanel.ColorPalette.DEFAULT));

        // Paint mode buttons
        paintOffBtn  .addActionListener(e -> listener.onPaintModeChange(GridPanel.PaintMode.OFF));
        paintABtn    .addActionListener(e -> listener.onPaintModeChange(GridPanel.PaintMode.PAINT_A));
        paintBBtn    .addActionListener(e -> listener.onPaintModeChange(GridPanel.PaintMode.PAINT_B));
        paintEraseBtn.addActionListener(e -> listener.onPaintModeChange(GridPanel.PaintMode.ERASE));
    }

    private void fireViewChange() {
        listener.onViewModeChange(heatmapCb.isSelected(), highlightCb.isSelected());
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public int getStepDelayMs() { return speedSlider.getValue(); }

    public void forceStop() {
        if (running) {
            running = false;
            runButton.setText("▶  Run");
            runButton.setBackground(new Color(45, 130, 70));
            stepButton.setEnabled(true);
        }
    }

    public void updateStats(int step, double sat, double isoA, double isoB,
                            double dissim, int moveCount, boolean stable) {
        stepLabel  .setText(String.format("Step:          %5d",   step));
        satLabel   .setText(String.format("Satisfaction: %5.1f%%", sat    * 100));
        isoALabel  .setText(String.format("Isolation A:  %5.1f%%", isoA   * 100));
        isoBLabel  .setText(String.format("Isolation B:  %5.1f%%", isoB   * 100));
        dissimLabel.setText(String.format("Dissimilarity:%5.2f",   dissim));
        movesLabel .setText(String.format("Last moves:   %5d",     moveCount));
        statusLabel.setText("Status: " + (stable ? "stable ✓" : "running"));
        statusLabel.setForeground(stable ? new Color(80, 200, 80) : FG_DIM);
    }

    public void appendLog(String message) {
        eventLog.append(message + "\n");
        // Auto-scroll to bottom
        eventLog.setCaretPosition(eventLog.getDocument().getLength());
    }

    public void clearLog() { eventLog.setText(""); }

    // ── Config builder ────────────────────────────────────────────────────────

    private SimulationConfig buildConfig(InitialCondition ic) {
        int    rows  = parseInt(rowsField.getText(),  SimulationConfig.DEFAULT_ROWS, 5, 200);
        int    cols  = parseInt(colsField.getText(),  SimulationConfig.DEFAULT_COLS, 5, 200);
        double tA    = threshASlider.getValue() / 100.0;
        double tB    = threshBSlider.getValue() / 100.0;
        double empty = Math.min(emptySlider.getValue() / 100.0, 0.90);
        long   seed  = parseLong(seedField.getText(), SimulationConfig.DEFAULT_RANDOM_SEED);
        NeighborhoodType nt = (NeighborhoodType) neighborhoodCombo.getSelectedItem();
        InitialCondition condition = (ic != null) ? ic : InitialCondition.RANDOM;

        return new SimulationConfig.Builder()
            .rows(rows).cols(cols)
            .thresholdA(tA).thresholdB(tB)
            .emptyFraction(empty)
            .randomSeed(seed)
            .neighborhoodType(nt)
            .initialCondition(condition)
            .build();
    }

    private static int parseInt(String s, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(s.trim()))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static long parseLong(String s, long fallback) {
        try { return Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    // ── Widget helpers ────────────────────────────────────────────────────────

    private static JPanel darkBox() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(18, 20, 32));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return p;
    }

    private static JScrollPane wrapScroll(JPanel inner) {
        JScrollPane sp = new JScrollPane(inner,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setBackground(new Color(18, 20, 32));
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private static JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(new Color(210, 220, 240));
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setFocusPainted(false);
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, b.getPreferredSize().height + 2));
        return b;
    }

    private static JButton miniButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(35, 40, 60));
        b.setForeground(new Color(160, 190, 240));
        b.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(30, 22));
        return b;
    }

    private static JToggleButton paintToggle(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setBackground(new Color(28, 32, 50));
        b.setForeground(new Color(170, 185, 215));
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 60, 90), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        return b;
    }

    private static JCheckBox styledCheckbox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setBackground(new Color(18, 20, 32));
        cb.setForeground(new Color(160, 180, 215));
        cb.setFont(SMALL);
        cb.setAlignmentX(LEFT_ALIGNMENT);
        return cb;
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(80, 160, 255));
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel dimLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(100, 115, 145));
        l.setFont(SMALL);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel statLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(185, 200, 230));
        l.setFont(MONO);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private static JSlider percentSlider(int initial) {
        JSlider s = new JSlider(0, 100, initial);
        s.setMajorTickSpacing(25);
        s.setMinorTickSpacing(5);
        s.setPaintTicks(true);
        s.setBackground(new Color(18, 20, 32));
        s.setForeground(new Color(80, 100, 140));
        s.setAlignmentX(LEFT_ALIGNMENT);
        return s;
    }

    private static JLabel valueLabel(JSlider slider, String unit) {
        JLabel l = new JLabel(slider.getValue() + unit);
        l.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 10));
        l.setForeground(new Color(130, 155, 200));
        l.setAlignmentX(LEFT_ALIGNMENT);
        slider.addChangeListener(e -> l.setText(slider.getValue() + unit));
        return l;
    }

    private static void styleTextField(JTextField tf) {
        tf.setBackground(new Color(25, 28, 44));
        tf.setForeground(new Color(190, 210, 240));
        tf.setFont(MONO);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 60, 90), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        tf.setCaretColor(new Color(180, 200, 240));
    }

    private static Component strut(int h) { return Box.createVerticalStrut(h); }

    private static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(38, 48, 72));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private static JPanel rowPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(new Color(18, 20, 32));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private static <T extends JComponent> T fillX(T c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        if (c.getMaximumSize().width < Integer.MAX_VALUE)
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 2));
        return c;
    }
}
