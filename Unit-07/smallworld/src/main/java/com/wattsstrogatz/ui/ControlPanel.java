package com.wattsstrogatz.ui;

import com.wattsstrogatz.model.NetworkConfig;
import com.wattsstrogatz.model.NetworkMetrics;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Sidebar panel with simulation controls, settings, live metrics, and legend.
 *
 * <p>Sections:
 * <ol>
 *   <li>Controls — Step 1, Step 10, Run/Pause, Reset, speed slider</li>
 *   <li>Settings — n, k, p (applied on Reset)</li>
 *   <li>Metrics  — MetricsPanel showing C/C0 and L/L0</li>
 *   <li>Legend   — edge colour key</li>
 * </ol>
 */
public final class ControlPanel extends JPanel {

    /** Callback interface for simulation events. */
    public interface SimulationListener {
        void onStep();
        void onStepMany(int count);
        void onRunToggled(boolean running);
        void onReset(NetworkConfig config);
    }

    // Preset configurations: { n, k, p*100 }
    private static final int[][] PRESETS = {
        { 30, 3,  0 },   // Regular lattice
        { 30, 3,  5 },   // Small-world regime
        { 30, 3, 50 },   // Near-random
        { 30, 3,100 },   // Fully random
    };
    private static final String[] PRESET_LABELS = {
        "Regular (p=0)", "Small-world (p=0.05)", "Near-random (p=0.5)", "Random (p=1)"
    };

    private final JButton      stepButton;
    private final JButton      step10Button;
    private final JButton      runButton;
    private final JButton      resetButton;
    private final JSlider      speedSlider;
    private final JTextField   nodeCountField;
    private final JTextField   kField;
    private final JSlider      pSlider;
    private final JLabel       pValueLabel;
    private final JLabel       progressLabel;
    private final MetricsPanel metricsPanel;

    private boolean running = false;
    private final SimulationListener listener;

    /**
     * Constructs the control panel.
     *
     * @param listener event callbacks; must not be null
     */
    public ControlPanel(SimulationListener listener) {
        if (listener == null) throw new NullPointerException("listener must not be null");
        this.listener = listener;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(235, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Controls -------------------------------------------------------
        JPanel ctrl = titled("Controls");
        stepButton   = btn("Step  1 edge",  new Color(70, 130, 180));
        step10Button = btn("Step 10 edges", new Color(80, 150, 200));
        runButton    = btn("▶  Run",         new Color(60, 160, 80));
        resetButton  = btn("Reset",          new Color(180, 80, 80));
        ctrl.add(stepButton);   ctrl.add(vgap(4));
        ctrl.add(step10Button); ctrl.add(vgap(4));
        ctrl.add(runButton);    ctrl.add(vgap(4));
        ctrl.add(resetButton);
        ctrl.add(vgap(8));
        ctrl.add(lbl("Run speed:   slow ◀──────▶ fast"));
        speedSlider = new JSlider(20, 500, 120);
        speedSlider.setInverted(true);   // value = delay ms; higher value = slower
        speedSlider.setMajorTickSpacing(200);
        speedSlider.setPaintTicks(true);
        ctrl.add(speedSlider);
        progressLabel = italic("Progress: 0 / 0 edges");
        ctrl.add(vgap(4));
        ctrl.add(progressLabel);

        // --- Settings -------------------------------------------------------
        JPanel settings = titled("Settings (apply on Reset)");
        settings.add(lbl("Nodes (n):"));
        nodeCountField = field(String.valueOf(NetworkConfig.DEFAULT_NODE_COUNT));
        settings.add(nodeCountField);
        settings.add(vgap(4));
        settings.add(lbl("Half-degree (k):"));
        kField = field(String.valueOf(NetworkConfig.DEFAULT_K));
        settings.add(kField);
        settings.add(vgap(6));
        settings.add(lbl("Rewiring probability (p):"));
        pSlider = new JSlider(0, 100, (int)(NetworkConfig.DEFAULT_REWIRING_PROBABILITY * 100));
        pSlider.setMajorTickSpacing(25);
        pSlider.setPaintTicks(true);
        pValueLabel = italic(fmtP(pSlider.getValue()));
        pSlider.addChangeListener(e -> pValueLabel.setText(fmtP(pSlider.getValue())));
        settings.add(pSlider);
        settings.add(pValueLabel);

        // --- Presets --------------------------------------------------------
        JPanel presets = titled("Presets (click to apply immediately)");
        JPanel presetGrid = new JPanel(new GridLayout(2, 2, 4, 4));
        presetGrid.setOpaque(false);
        presetGrid.setAlignmentX(LEFT_ALIGNMENT);
        presetGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        for (int i = 0; i < PRESET_LABELS.length; i++) {
            final int idx = i;
            JButton pb = new JButton("<html><center>" + PRESET_LABELS[i].replace(" (", "<br><small>(") + "</small></center></html>");
            pb.setFont(new Font("SansSerif", Font.PLAIN, 10));
            pb.setFocusPainted(false);
            pb.addActionListener(e -> applyPreset(PRESETS[idx][0], PRESETS[idx][1], PRESETS[idx][2] / 100.0));
            presetGrid.add(pb);
        }
        presets.add(presetGrid);

        // --- Metrics --------------------------------------------------------
        JPanel metricsContainer = titled("Metrics");
        metricsPanel = new MetricsPanel();
        metricsPanel.setAlignmentX(LEFT_ALIGNMENT);
        metricsContainer.add(metricsPanel);

        // --- Legend ---------------------------------------------------------
        JPanel legend = titled("Legend");
        legend.add(legendRow(new Color(160, 170, 185), "Lattice edge"));
        legend.add(vgap(2));
        legend.add(legendRow(new Color(220, 80, 50),   "Rewired edge"));
        legend.add(vgap(2));
        legend.add(legendRow(new Color(50, 100, 160),  "Node"));

        add(ctrl);             add(vgap(8));
        add(settings);         add(vgap(8));
        add(presets);          add(vgap(8));
        add(metricsContainer); add(vgap(8));
        add(legend);
        add(Box.createVerticalGlue());

        // --- Wire actions ---------------------------------------------------
        stepButton.addActionListener(e -> { if (!running) listener.onStep(); });
        step10Button.addActionListener(e -> { if (!running) listener.onStepMany(10); });
        runButton.addActionListener(e -> {
            running = !running;
            runButton.setText(running ? "⏸  Pause" : "▶  Run");
            stepButton.setEnabled(!running);
            step10Button.setEnabled(!running);
            listener.onRunToggled(running);
        });
        resetButton.addActionListener(e -> {
            if (running) {
                running = false;
                runButton.setText("▶  Run");
                stepButton.setEnabled(true);
                step10Button.setEnabled(true);
                listener.onRunToggled(false);
            }
            listener.onReset(buildConfig());
        });
    }

    /** @return auto-step delay in milliseconds */
    public int getStepDelayMs() { return speedSlider.getValue(); }

    /**
     * Updates progress label and metrics panel.
     *
     * @param visited  edges visited so far
     * @param total    total edges
     * @param current  raw current metrics
     * @param relative normalised metrics
     * @param complete whether rewiring is complete
     */
    public void updateStats(int visited, int total,
                            NetworkMetrics.MetricsSnapshot current,
                            NetworkMetrics.MetricsSnapshot relative,
                            boolean complete) {
        progressLabel.setText(String.format("Progress: %d / %d edges%s",
            visited, total, complete ? " ✓" : ""));
        metricsPanel.update(current, relative,
            total == 0 ? 0.0 : (double) visited / total);
        metricsPanel.repaint();
    }

    /** Stops auto-run state (called when rewiring completes). */
    public void forceStop() {
        if (running) {
            running = false;
            runButton.setText("▶  Run");
            stepButton.setEnabled(true);
            step10Button.setEnabled(true);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void applyPreset(int n, int k, double p) {
        if (running) {
            running = false;
            runButton.setText("▶  Run");
            stepButton.setEnabled(true);
            step10Button.setEnabled(true);
            listener.onRunToggled(false);
        }
        nodeCountField.setText(String.valueOf(n));
        kField.setText(String.valueOf(k));
        pSlider.setValue((int) Math.round(p * 100));
        listener.onReset(buildConfig());
    }

    private NetworkConfig buildConfig() {
        int n = clamped(nodeCountField.getText(), NetworkConfig.DEFAULT_NODE_COUNT, 6, 250);
        int k = clamped(kField.getText(), NetworkConfig.DEFAULT_K, 1, 20);
        if (n <= 2 * k) {
            k = Math.max(1, (n - 1) / 2);
            kField.setText(String.valueOf(k));
        }
        double p = pSlider.getValue() / 100.0;
        return new NetworkConfig.Builder()
            .nodeCount(n).k(k).rewiringProbability(p)
            .randomSeed(System.currentTimeMillis()).build();
    }

    private static int clamped(String text, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(text.trim()))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static String fmtP(int v) { return String.format("p = %.2f", v / 100.0); }

    private static JPanel titled(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 11)));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private static JLabel lbl(String text) {
        JLabel l = new JLabel(text); l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }

    private static JLabel italic(String text) {
        JLabel l = lbl(text);
        l.setFont(l.getFont().deriveFont(Font.ITALIC, 11f));
        return l;
    }

    private static JTextField field(String value) {
        JTextField f = new JTextField(value, 5);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, f.getPreferredSize().height));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    private static JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, b.getPreferredSize().height));
        return b;
    }

    private static Component vgap(int h) { return Box.createVerticalStrut(h); }

    private static JPanel legendRow(Color color, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sw = new JLabel("     ");
        sw.setOpaque(true); sw.setBackground(color);
        sw.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        row.add(sw); row.add(new JLabel(text));
        return row;
    }
}
