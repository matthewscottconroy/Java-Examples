package com.schelling.ui;

import com.schelling.model.AgentType;
import com.schelling.model.SimulationConfig;
import com.schelling.simulation.SchellingSimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.Supplier;

/**
 * Phase diagram panel: sweeps (satisfaction threshold × empty fraction) and
 * shows the resulting segregation level as a 2D heat-map.
 *
 * <p>The sweep runs in a background {@link SwingWorker} so the UI stays
 * responsive.  Each cell of the grid runs a simulation to convergence (or
 * a maximum step count) and records the isolation index of Group A.
 *
 * <ul>
 *   <li>X axis — satisfaction threshold (both groups equal), 0 → 80 %</li>
 *   <li>Y axis — empty-cell fraction, 5 → 55 %</li>
 *   <li>Color — blue (0 = fully integrated) → red (1 = fully segregated)</li>
 * </ul>
 *
 * <p>A dashed crosshair marks the current simulation's parameter point.
 * Call {@link #setCurrentParams(double, double)} whenever they change.
 */
public final class PhaseDiagramPanel extends JPanel {

    // ── Sweep parameters ──────────────────────────────────────────────────────
    private static final int    THRESHOLD_STEPS = 14;
    private static final int    EMPTY_STEPS     = 10;
    private static final double THRESHOLD_MIN   = 0.05;
    private static final double THRESHOLD_MAX   = 0.75;
    private static final double EMPTY_MIN       = 0.05;
    private static final double EMPTY_MAX       = 0.55;
    private static final int    GRID_SIZE       = 40;
    private static final int    MAX_STEPS       = 200;

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final Color BG       = new Color(14, 16, 26);
    private static final Color AXIS_COL = new Color(80, 95, 130);
    private static final Color TEXT_COL = new Color(160, 175, 210);
    private static final int   PAD_L    = 52;
    private static final int   PAD_R    = 20;
    private static final int   PAD_T    = 22;
    private static final int   PAD_B    = 38;
    private static final int   BAR_W    = 16;

    // ── State ─────────────────────────────────────────────────────────────────
    private double[][]    results;          // [threshIdx][emptyIdx] = isolation
    private boolean       sweepRunning = false;
    private int           progress     = 0;  // cells completed
    private int           total        = THRESHOLD_STEPS * EMPTY_STEPS;
    private double        currentThreshold = SimulationConfig.DEFAULT_THRESHOLD_A;
    private double        currentEmpty     = SimulationConfig.DEFAULT_EMPTY_FRACTION;
    private Supplier<SimulationConfig> configSupplier;

    private final JButton    runButton;
    private final JLabel     statusLabel;
    private SwingWorker<double[][], Integer> worker;

    // ── Constructor ───────────────────────────────────────────────────────────

    public PhaseDiagramPanel() {
        setBackground(BG);
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        top.setBackground(BG);

        JLabel title = new JLabel("Segregation Phase Diagram");
        title.setForeground(new Color(140, 180, 255));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        top.add(title);

        runButton = new JButton("Run Sweep");
        styleButton(runButton);
        runButton.addActionListener(e -> runSweep());
        top.add(runButton);

        statusLabel = new JLabel("Click 'Run Sweep' to compute");
        statusLabel.setForeground(new Color(110, 120, 145));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        top.add(statusLabel);

        add(top, BorderLayout.NORTH);

        // The chart fills the center
        JPanel chartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(BG);
                paintChart((Graphics2D) g);
            }
        };
        chartPanel.setBackground(BG);
        add(chartPanel, BorderLayout.CENTER);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void setConfigSupplier(Supplier<SimulationConfig> s) { configSupplier = s; }

    public void setCurrentParams(double threshold, double empty) {
        this.currentThreshold = threshold;
        this.currentEmpty     = empty;
        repaint();
    }

    public JButton getRunButton() { return runButton; }

    // ── Sweep ─────────────────────────────────────────────────────────────────

    private void runSweep() {
        if (sweepRunning) return;
        sweepRunning = true;
        progress     = 0;
        results      = new double[THRESHOLD_STEPS][EMPTY_STEPS];
        runButton.setEnabled(false);
        statusLabel.setText("Running…  0 / " + total);

        SimulationConfig base = (configSupplier != null)
            ? configSupplier.get()
            : SimulationConfig.defaults();

        worker = new SwingWorker<>() {
            @Override protected double[][] doInBackground() {
                double[][] res = new double[THRESHOLD_STEPS][EMPTY_STEPS];
                for (int ti = 0; ti < THRESHOLD_STEPS; ti++) {
                    double thr = THRESHOLD_MIN + ti * (THRESHOLD_MAX - THRESHOLD_MIN)
                                 / (THRESHOLD_STEPS - 1);
                    for (int ei = 0; ei < EMPTY_STEPS; ei++) {
                        if (isCancelled()) return res;
                        double empty = EMPTY_MIN + ei * (EMPTY_MAX - EMPTY_MIN)
                                       / (EMPTY_STEPS - 1);
                        res[ti][ei] = runTrial(base, thr, empty);
                        publish(1);  // signal one cell done
                    }
                }
                return res;
            }

            @Override protected void process(java.util.List<Integer> chunks) {
                progress += chunks.stream().mapToInt(Integer::intValue).sum();
                statusLabel.setText("Running…  " + progress + " / " + total);
                results = getCurrentPartialResults();
                repaint();
            }

            private double[][] partialResults = new double[THRESHOLD_STEPS][EMPTY_STEPS];
            private double[][] getCurrentPartialResults() { return partialResults; }

            @Override protected void done() {
                try {
                    results = get();
                } catch (Exception ex) { /* cancelled */ }
                sweepRunning = false;
                runButton.setEnabled(true);
                statusLabel.setText("Done — " + THRESHOLD_STEPS * EMPTY_STEPS + " points computed");
                repaint();
            }
        };
        worker.execute();
    }

    private static double runTrial(SimulationConfig base, double thr, double empty) {
        SimulationConfig cfg = new SimulationConfig.Builder()
            .rows(GRID_SIZE).cols(GRID_SIZE)
            .satisfactionThreshold(thr)
            .emptyFraction(empty)
            .randomSeed(base.getRandomSeed())
            .neighborhoodType(base.getNeighborhoodType())
            .typeBFraction(base.getTypeBFraction())
            .build();
        SchellingSimulation sim = new SchellingSimulation(cfg);
        for (int s = 0; s < MAX_STEPS && !sim.isStable(); s++) sim.step();
        return sim.getIsolationIndex(AgentType.TYPE_A);
    }

    // ── Chart rendering ───────────────────────────────────────────────────────

    private void paintChart(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int W = getWidth(), H = getHeight();
        if (W < 100 || H < 80) return;

        int cx = PAD_L, cy = PAD_T;
        int cw = W - PAD_L - PAD_R - BAR_W - 6;
        int ch = H - PAD_T - PAD_B;
        if (cw < 20 || ch < 20) return;

        // Heat-map cells
        if (results != null) {
            int cellW = cw / THRESHOLD_STEPS;
            int cellH = ch / EMPTY_STEPS;
            for (int ti = 0; ti < THRESHOLD_STEPS; ti++) {
                for (int ei = 0; ei < EMPTY_STEPS; ei++) {
                    double val = results[ti][ei];
                    g.setColor(heatColor(val));
                    int x = cx + ti * cellW;
                    // y: ei=0 is lowest empty → bottom of chart
                    int y = cy + (EMPTY_STEPS - 1 - ei) * cellH;
                    g.fillRect(x, y, cellW, cellH);
                }
            }
        } else {
            g.setColor(new Color(25, 30, 48));
            g.fillRect(cx, cy, cw, ch);
            g.setColor(new Color(70, 85, 120));
            g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
            String msg = "Click 'Run Sweep' to compute";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, cx + (cw - fm.stringWidth(msg)) / 2, cy + ch / 2);
        }

        // Axes
        g.setColor(AXIS_COL);
        g.drawRect(cx, cy, cw, ch);

        // Axis labels & ticks
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g.setColor(TEXT_COL);
        FontMetrics fm = g.getFontMetrics();

        // X axis (threshold)
        for (int ti = 0; ti < THRESHOLD_STEPS; ti += 3) {
            double thr = THRESHOLD_MIN + ti * (THRESHOLD_MAX - THRESHOLD_MIN)
                         / (THRESHOLD_STEPS - 1);
            int x = cx + ti * (cw / THRESHOLD_STEPS) + cw / THRESHOLD_STEPS / 2;
            String label = (int)(thr * 100) + "%";
            g.drawString(label, x - fm.stringWidth(label) / 2, cy + ch + 12);
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        String xTitle = "Threshold";
        g.drawString(xTitle, cx + (cw - fm.stringWidth(xTitle)) / 2, cy + ch + 26);

        // Y axis (empty fraction)
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        for (int ei = 0; ei < EMPTY_STEPS; ei += 2) {
            double empty = EMPTY_MIN + ei * (EMPTY_MAX - EMPTY_MIN) / (EMPTY_STEPS - 1);
            int y = cy + (EMPTY_STEPS - 1 - ei) * (ch / EMPTY_STEPS) + ch / EMPTY_STEPS / 2;
            String label = (int)(empty * 100) + "%";
            g.drawString(label, cx - fm.stringWidth(label) - 3, y + 4);
        }
        // Y axis title (rotated)
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        Graphics2D gr = (Graphics2D) g.create();
        gr.rotate(-Math.PI / 2, 12, cy + ch / 2);
        gr.drawString("Empty %", 12 - fm.stringWidth("Empty %") / 2, cy + ch / 2);
        gr.dispose();

        // Crosshair for current params
        drawCrosshair(g, cx, cy, cw, ch);

        // Color bar
        drawColorBar(g, cx + cw + 6, cy, BAR_W, ch);
    }

    private void drawCrosshair(Graphics2D g, int cx, int cy, int cw, int ch) {
        double thrFrac   = (currentThreshold - THRESHOLD_MIN) / (THRESHOLD_MAX - THRESHOLD_MIN);
        double emptyFrac = (currentEmpty     - EMPTY_MIN)     / (EMPTY_MAX     - EMPTY_MIN);
        thrFrac   = Math.max(0, Math.min(1, thrFrac));
        emptyFrac = Math.max(0, Math.min(1, emptyFrac));

        int px = cx + (int)(thrFrac   * cw);
        int py = cy + (int)((1 - emptyFrac) * ch);

        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 1, new float[]{4, 3}, 0));
        g.setColor(new Color(255, 255, 255, 200));
        g.drawLine(px, cy, px, cy + ch);
        g.drawLine(cx, py, cx + cw, py);
        g.setStroke(old);

        g.setColor(Color.WHITE);
        g.fillOval(px - 3, py - 3, 6, 6);
    }

    private void drawColorBar(Graphics2D g, int x, int y, int w, int h) {
        for (int i = 0; i < h; i++) {
            double val = 1.0 - (double) i / h;
            g.setColor(heatColor(val));
            g.fillRect(x, y + i, w, 1);
        }
        g.setColor(AXIS_COL);
        g.drawRect(x, y, w, h);

        g.setColor(TEXT_COL);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
        g.drawString("1", x + w + 2, y + 8);
        g.drawString("0", x + w + 2, y + h);
    }

    /** Blue (0 = integrated) → red (1 = segregated). */
    private static Color heatColor(double v) {
        v = Math.max(0, Math.min(1, v));
        int r = (int)(v * 220 + (1 - v) * 30);
        int gr = (int)(v * 40  + (1 - v) * 80);
        int b  = (int)(v * 40  + (1 - v) * 200);
        return new Color(r, gr, b);
    }

    private static void styleButton(JButton b) {
        b.setBackground(new Color(35, 50, 80));
        b.setForeground(new Color(160, 200, 255));
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 90, 140), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
    }
}
