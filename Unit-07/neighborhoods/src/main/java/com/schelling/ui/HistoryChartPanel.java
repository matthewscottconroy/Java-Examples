package com.schelling.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Embedded time-series chart showing per-step simulation history.
 *
 * <h2>Layout</h2>
 * <pre>
 *  ┌──────────────────────────────────────────────────────┐
 *  │  ── Satisfaction %  ── IsolationA  ── IsolationB     │ ← legend
 *  │                                                      │
 *  │  [line chart: 0-100% on Y, step on X]               │
 *  │                                                      │
 *  │  [move count bars at bottom strip]                   │
 *  └──────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>Call {@link #record(int, double, double, double, int)} after each step,
 * and {@link #clear()} on reset.
 */
public final class HistoryChartPanel extends JPanel {

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(14, 16, 26);
    private static final Color GRID_LINE   = new Color(30, 38, 60);
    private static final Color SAT_COLOR   = new Color(80, 170, 255);
    private static final Color ISO_A_COLOR = new Color(120, 200, 255);
    private static final Color ISO_B_COLOR = new Color(240, 110, 110);
    private static final Color MOVES_COLOR = new Color(200, 160,  50);
    private static final Color AXIS_COLOR  = new Color(80,  95, 130);
    private static final Color TEXT_COLOR  = new Color(160, 175, 210);

    // ── Data ──────────────────────────────────────────────────────────────────
    private final List<Integer> steps        = new ArrayList<>();
    private final List<Double>  satisfaction = new ArrayList<>();
    private final List<Double>  isolationA   = new ArrayList<>();
    private final List<Double>  isolationB   = new ArrayList<>();
    private final List<Integer> moves        = new ArrayList<>();

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int PAD_L  = 38;
    private static final int PAD_R  = 12;
    private static final int PAD_T  = 22;   // legend height
    private static final int PAD_B  = 20;
    private static final int MOVE_H = 28;   // height of moves strip at bottom

    // ── Constructor ───────────────────────────────────────────────────────────

    public HistoryChartPanel() {
        setBackground(BG);
        setPreferredSize(new Dimension(0, 170));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 80)));
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Records one step's metrics.
     *
     * @param step         step number
     * @param satisfaction overall satisfaction rate [0,1]
     * @param isoA         isolation index for Group A [0,1]
     * @param isoB         isolation index for Group B [0,1]
     * @param moveCount    number of agents that moved
     */
    public void record(int step, double satisfaction,
                       double isoA, double isoB, int moveCount) {
        this.steps       .add(step);
        this.satisfaction.add(satisfaction);
        this.isolationA  .add(isoA);
        this.isolationB  .add(isoB);
        this.moves       .add(moveCount);
        repaint();
    }

    /** Clears all recorded history (called on reset). */
    public void clear() {
        steps.clear(); satisfaction.clear();
        isolationA.clear(); isolationB.clear(); moves.clear();
        repaint();
    }

    public int getStepCount() { return steps.size(); }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // Chart area bounds
        int cx = PAD_L, cy = PAD_T;
        int cw = W - PAD_L - PAD_R;
        int ch = H - PAD_T - PAD_B - MOVE_H;   // main line chart height
        int my = cy + ch + 4;                    // moves strip Y start
        int mh = MOVE_H - 4;                     // moves strip height

        drawLegend(g, cx, 4);
        if (steps.isEmpty()) {
            g.setColor(new Color(70, 85, 120));
            g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
            String msg = "Run the simulation to see metrics over time";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, cx + (cw - fm.stringWidth(msg)) / 2, cy + ch / 2);
            return;
        }

        drawGrid(g, cx, cy, cw, ch);
        drawLines(g, cx, cy, cw, ch);
        drawMoves(g, cx, my, cw, mh);
        drawAxes(g, cx, cy, cw, ch, my, mh);
    }

    private void drawLegend(Graphics2D g, int x, int y) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        int lineLen = 16, gap = 6, itemGap = 14;
        String[] labels = {"Satisfaction", "Isolation A", "Isolation B"};
        Color[]  colors = {SAT_COLOR, ISO_A_COLOR, ISO_B_COLOR};

        int tx = x;
        for (int i = 0; i < labels.length; i++) {
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(i == 0 ? 2f : 1.5f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, i == 0 ? null : new float[]{4, 3}, 0));
            g.setColor(colors[i]);
            int ly = y + fm.getAscent() / 2 + 1;
            g.drawLine(tx, ly, tx + lineLen, ly);
            g.setStroke(old);
            g.setColor(TEXT_COLOR);
            g.drawString(labels[i], tx + lineLen + gap, y + fm.getAscent());
            tx += lineLen + gap + fm.stringWidth(labels[i]) + itemGap;
        }
        // Moves swatch
        g.setColor(MOVES_COLOR);
        g.fillRect(tx, y + 2, 8, fm.getAscent() - 2);
        g.setColor(TEXT_COLOR);
        g.drawString("Moves", tx + 10, y + fm.getAscent());
    }

    private void drawGrid(Graphics2D g, int cx, int cy, int cw, int ch) {
        g.setColor(GRID_LINE);
        g.setStroke(new BasicStroke(0.5f));
        for (int pct = 0; pct <= 100; pct += 25) {
            int y = cy + ch - (int)(pct / 100.0 * ch);
            g.drawLine(cx, y, cx + cw, y);
        }
    }

    private void drawLines(Graphics2D g, int cx, int cy, int cw, int ch) {
        int n = steps.size();
        if (n < 2) return;

        int maxStep = steps.get(n - 1);

        drawSeries(g, cx, cy, cw, ch, n, maxStep, satisfaction,
            SAT_COLOR, new BasicStroke(2f));
        drawSeries(g, cx, cy, cw, ch, n, maxStep, isolationA,
            ISO_A_COLOR, new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 0, new float[]{5, 3}, 0));
        drawSeries(g, cx, cy, cw, ch, n, maxStep, isolationB,
            ISO_B_COLOR, new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 0, new float[]{5, 3}, 0));
    }

    private void drawSeries(Graphics2D g, int cx, int cy, int cw, int ch,
                             int n, int maxStep, List<Double> data,
                             Color color, Stroke stroke) {
        Path2D path = new Path2D.Double();
        boolean started = false;
        for (int i = 0; i < n; i++) {
            double sx = maxStep > 0 ? (double) steps.get(i) / maxStep : 0;
            double sy = data.get(i);
            int px = cx + (int)(sx * cw);
            int py = cy + ch - (int)(sy * ch);
            if (!started) { path.moveTo(px, py); started = true; }
            else            path.lineTo(px, py);
        }
        Stroke old = g.getStroke();
        g.setStroke(stroke);
        g.setColor(color);
        g.draw(path);
        g.setStroke(old);
    }

    private void drawMoves(Graphics2D g, int cx, int my, int cw, int mh) {
        int n = steps.size();
        if (n == 0) return;
        int maxMoves = moves.stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxMoves == 0) maxMoves = 1;
        int maxStep  = steps.get(n - 1);
        int barW     = Math.max(1, cw / Math.max(n, 1));

        g.setColor(MOVES_COLOR);
        for (int i = 0; i < n; i++) {
            double sx = maxStep > 0 ? (double) steps.get(i) / maxStep : 0;
            int px  = cx + (int)(sx * cw);
            int barH = (int)((double) moves.get(i) / maxMoves * mh);
            g.fillRect(px - barW / 2, my + mh - barH, Math.max(1, barW - 1), barH);
        }
    }

    private void drawAxes(Graphics2D g, int cx, int cy, int cw, int ch,
                           int my, int mh) {
        g.setColor(AXIS_COLOR);
        g.setStroke(new BasicStroke(1f));
        // Y axis
        g.drawLine(cx, cy, cx, cy + ch);
        // X axis
        g.drawLine(cx, cy + ch, cx + cw, cy + ch);

        // Y tick labels (0 / 50% / 100%)
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
        g.setColor(TEXT_COLOR);
        FontMetrics fm = g.getFontMetrics();
        for (int pct : new int[]{0, 50, 100}) {
            int y = cy + ch - (int)(pct / 100.0 * ch);
            g.drawString(pct + "%", cx - fm.stringWidth(pct + "%") - 3, y + 4);
        }

        // X axis label
        int n = steps.size();
        if (n > 0) {
            String xLabel = "Step " + steps.get(n - 1);
            g.drawString(xLabel, cx + cw - fm.stringWidth(xLabel), cy + ch + 13);
            g.drawString("0", cx, cy + ch + 13);
        }
    }
}
