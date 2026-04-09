package com.wattsstrogatz.ui;

import com.wattsstrogatz.model.NetworkConfig;
import com.wattsstrogatz.model.NetworkMetrics;
import com.wattsstrogatz.simulation.WattsStrogatzSimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Displays the hallmark Watts-Strogatz phase diagram: normalised clustering
 * coefficient C(p)/C(0) and average path length L(p)/L(0) plotted against
 * the rewiring probability p on a logarithmic x-axis.
 *
 * <p>The small-world regime is the region where L drops sharply while C
 * remains close to 1 — typically around p ≈ 0.01–0.1. A vertical dashed
 * line marks the simulation's current p value.
 *
 * <p>Click <b>Run Sweep</b> to compute the curves across 20 log-spaced
 * p values (3 independent runs each, averaged for smoothness). The sweep
 * runs in a background thread so the UI stays responsive.
 */
public final class PhaseDiagramPanel extends JPanel {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final Color C_COLOR     = new Color(70, 140, 200);
    private static final Color L_COLOR     = new Color(210, 90,  60);
    private static final Color GRID_COLOR  = new Color(218, 223, 230);
    private static final Color AXIS_COLOR  = new Color(90, 95, 110);
    private static final Color PANEL_BG    = new Color(245, 245, 248);
    private static final Color CHART_BG    = Color.WHITE;

    private static final double P_MIN        = 1e-3;
    private static final double P_MAX        = 1.0;
    private static final double Y_MAX        = 1.1;   // y-axis ceiling
    private static final int    SWEEP_POINTS = 20;
    private static final int    RUNS_PER_P   = 3;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Sweep results: each element is double[] {p, relC, relL}. */
    private final List<double[]> sweepData = new ArrayList<>();
    private double currentP = NetworkConfig.DEFAULT_REWIRING_PROBABILITY;
    private boolean sweeping = false;
    private Supplier<NetworkConfig> configSupplier;
    private SwingWorker<Void, double[]> sweepWorker;

    // -------------------------------------------------------------------------
    // UI components
    // -------------------------------------------------------------------------

    private final JButton sweepButton;
    private final JLabel  statusLabel;
    private final JPanel  chartPanel;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public PhaseDiagramPanel() {
        setLayout(new BorderLayout(4, 2));
        setBackground(PANEL_BG);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(195, 200, 210)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        // Top bar: title + button + status
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("Phase Diagram — C(p)/C(0) and L(p)/L(0) vs p  (log scale)");
        title.setFont(new Font("SansSerif", Font.BOLD, 11));

        sweepButton = new JButton("Run Sweep");
        sweepButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sweepButton.setFocusPainted(false);
        sweepButton.addActionListener(e -> startSweep());

        statusLabel = new JLabel("No sweep data — click Run Sweep");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(100, 105, 115));

        topBar.add(title);
        topBar.add(sweepButton);
        topBar.add(statusLabel);

        // Chart area
        chartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                paintChart(g2, getWidth(), getHeight());
            }
        };
        chartPanel.setOpaque(false);

        add(topBar,    BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(0, 185));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Supplies the current simulation config so the sweep knows n and k.
     *
     * @param supplier config supplier; must not be null
     */
    public void setConfigSupplier(Supplier<NetworkConfig> supplier) {
        this.configSupplier = supplier;
    }

    /**
     * Updates the vertical p-marker.  Does not repaint.
     *
     * @param p current rewiring probability
     */
    public void setCurrentP(double p) {
        this.currentP = p;
    }

    /** @return the Run Sweep button (allows external triggers, e.g. from the menu bar). */
    public JButton getRunSweepButton() { return sweepButton; }

    /**
     * Clears sweep data.  Call when n or k changes so stale curves are not
     * shown for the new configuration.
     */
    public void clearSweep() {
        if (sweepWorker != null && !sweepWorker.isDone()) sweepWorker.cancel(true);
        sweeping = false;
        sweepData.clear();
        sweepButton.setEnabled(true);
        statusLabel.setText("No sweep data — click Run Sweep");
        chartPanel.repaint();
    }

    // -------------------------------------------------------------------------
    // Sweep
    // -------------------------------------------------------------------------

    private void startSweep() {
        if (sweeping || configSupplier == null) return;

        NetworkConfig cfg = configSupplier.get();
        final int n = cfg.getNodeCount(), k = cfg.getK();

        sweeping = true;
        sweepButton.setEnabled(false);
        sweepData.clear();

        double[] pValues = logSpaced(P_MIN, P_MAX, SWEEP_POINTS);

        sweepWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (int i = 0; i < pValues.length && !isCancelled(); i++) {
                    double p = pValues[i];
                    double sumC = 0, sumL = 0;
                    for (int run = 0; run < RUNS_PER_P; run++) {
                        NetworkConfig runCfg = new NetworkConfig.Builder()
                            .nodeCount(n).k(k).rewiringProbability(p)
                            .randomSeed((long) run * 997 + i * 31L).build();
                        WattsStrogatzSimulation sim = new WattsStrogatzSimulation(runCfg);
                        sim.stepAll();
                        NetworkMetrics.MetricsSnapshot rel = sim.getRelativeMetrics();
                        sumC += rel.getClusteringCoefficient();
                        double lv = rel.getAvgPathLength();
                        sumL += Double.isFinite(lv) ? lv : 1.0;
                    }
                    publish(new double[]{p, sumC / RUNS_PER_P, sumL / RUNS_PER_P});
                }
                return null;
            }

            @Override
            protected void process(List<double[]> chunks) {
                sweepData.addAll(chunks);
                statusLabel.setText(String.format(
                    "Sweeping: %d / %d  (n=%d, k=%d)", sweepData.size(), SWEEP_POINTS, n, k));
                chartPanel.repaint();
            }

            @Override
            protected void done() {
                sweeping = false;
                sweepButton.setEnabled(true);
                if (!isCancelled()) {
                    statusLabel.setText(String.format(
                        "n=%d, k=%d — %d p-values × %d runs each", n, k, SWEEP_POINTS, RUNS_PER_P));
                }
                chartPanel.repaint();
            }
        };
        sweepWorker.execute();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    private void paintChart(Graphics2D g2, int w, int h) {
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int left   = 44, top    = 6;
            int right  = w - 10, bottom = h - 26;
            int chartW = right - left, chartH = bottom - top;
            if (chartW <= 10 || chartH <= 10) return;

            // Chart background
            g2.setColor(CHART_BG);
            g2.fillRect(left, top, chartW, chartH);

            // Horizontal grid lines at Y = 0.25, 0.5, 0.75, 1.0
            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(0.5f));
            for (double yv : new double[]{0.25, 0.5, 0.75, 1.0}) {
                int py = chartY(yv, top, bottom);
                g2.drawLine(left, py, right, py);
            }

            // Vertical grid lines at decade marks
            for (double logP : new double[]{-3, -2, -1, 0}) {
                int px = chartX(Math.pow(10, logP), left, right);
                g2.drawLine(px, top, px, bottom);
            }

            // Chart border
            g2.setColor(AXIS_COLOR);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRect(left, top, chartW, chartH);

            // Y-axis labels
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g2.setColor(AXIS_COLOR);
            FontMetrics fm = g2.getFontMetrics();
            for (double yv : new double[]{0.0, 0.5, 1.0}) {
                String lbl = String.format("%.1f", yv);
                int py = chartY(yv, top, bottom);
                g2.drawString(lbl, left - fm.stringWidth(lbl) - 3, py + 4);
            }

            // X-axis labels
            double[] xPs     = {0.001, 0.01, 0.1, 1.0};
            String[] xLabels = {"0.001", "0.01", "0.1", "1"};
            for (int i = 0; i < xPs.length; i++) {
                int px = chartX(xPs[i], left, right);
                int lw = fm.stringWidth(xLabels[i]);
                g2.drawString(xLabels[i], px - lw / 2, bottom + 14);
            }

            // X-axis title
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            String xTitle = "rewiring probability p";
            g2.drawString(xTitle, left + (chartW - fm.stringWidth(xTitle)) / 2, bottom + 24);

            // Current p — vertical dashed line
            if (currentP >= P_MIN && currentP <= P_MAX) {
                int px = chartX(currentP, left, right);
                g2.setColor(new Color(120, 120, 180, 170));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[]{5f, 3f}, 0f));
                g2.drawLine(px, top, px, bottom);
            }

            // Data curves
            if (sweepData.size() >= 2) {
                drawCurve(g2, 1, C_COLOR, 2.0f, left, right, top, bottom);  // C(p)/C(0)
                drawCurve(g2, 2, L_COLOR, 2.0f, left, right, top, bottom);  // L(p)/L(0)
            } else if (sweepData.isEmpty()) {
                // Placeholder hint text
                g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
                g2.setColor(new Color(160, 165, 175));
                String hint = "Run Sweep to see C(p)/C(0) and L(p)/L(0) curves";
                g2.drawString(hint, left + (chartW - g2.getFontMetrics().stringWidth(hint)) / 2,
                    top + chartH / 2 + 4);
            }

            // Legend
            paintLegend(g2, left + 6, top + 8);

        } finally {
            g2.dispose();
        }
    }

    private void paintLegend(Graphics2D g2, int lx, int ly) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        // C line
        g2.setColor(C_COLOR);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine(lx, ly, lx + 18, ly);
        g2.setColor(AXIS_COLOR);
        g2.drawString("C(p)/C(0)", lx + 22, ly + fm.getAscent() / 2);

        // L line
        int ly2 = ly + fm.getHeight() + 2;
        g2.setColor(L_COLOR);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawLine(lx, ly2, lx + 18, ly2);
        g2.setColor(AXIS_COLOR);
        g2.drawString("L(p)/L(0)", lx + 22, ly2 + fm.getAscent() / 2);

        // current-p marker
        int ly3 = ly2 + fm.getHeight() + 2;
        g2.setColor(new Color(120, 120, 180, 200));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
            1f, new float[]{5f, 3f}, 0f));
        g2.drawLine(lx, ly3, lx + 18, ly3);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(AXIS_COLOR);
        g2.drawString("current p", lx + 22, ly3 + fm.getAscent() / 2);
    }

    private void drawCurve(Graphics2D g2, int col, Color color, float stroke,
                           int left, int right, int top, int bottom) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D path = new Path2D.Double();
        boolean started = false;
        for (double[] pt : sweepData) {
            double v = pt[col];
            if (!Double.isFinite(v)) continue;
            int px = chartX(pt[0], left, right);
            int py = chartY(v,     top,  bottom);
            if (!started) { path.moveTo(px, py); started = true; }
            else            path.lineTo(px, py);
        }
        if (started) g2.draw(path);

        // Dot markers
        g2.setStroke(new BasicStroke(1f));
        for (double[] pt : sweepData) {
            double v = pt[col];
            if (!Double.isFinite(v)) continue;
            int px = chartX(pt[0], left, right);
            int py = chartY(v,     top,  bottom);
            g2.setColor(Color.WHITE);
            g2.fillOval(px - 3, py - 3, 6, 6);
            g2.setColor(color);
            g2.drawOval(px - 3, py - 3, 6, 6);
        }
    }

    // -------------------------------------------------------------------------
    // Coordinate helpers
    // -------------------------------------------------------------------------

    private int chartX(double p, int left, int right) {
        double lp   = Math.log10(Math.max(p, P_MIN));
        double lmin = Math.log10(P_MIN);
        double lmax = Math.log10(P_MAX);
        return left + (int) Math.round((lp - lmin) / (lmax - lmin) * (right - left));
    }

    private int chartY(double v, int top, int bottom) {
        double frac = Math.max(0, Math.min(v, Y_MAX)) / Y_MAX;
        return bottom - (int) Math.round(frac * (bottom - top));
    }

    private static double[] logSpaced(double min, double max, int count) {
        double[] vals = new double[count];
        double lmin = Math.log10(min), lmax = Math.log10(max);
        for (int i = 0; i < count; i++)
            vals[i] = Math.pow(10, lmin + (double) i / (count - 1) * (lmax - lmin));
        return vals;
    }
}
