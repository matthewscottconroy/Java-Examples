package com.epidemic.ui;

import com.epidemic.model.SIROde;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simulation canvas for the aggregate ODE SIR model.
 *
 * <p>Maintains a rolling history of the last {@value #MAX_HISTORY} time steps
 * and renders the S(t), I(t), and R(t) curves as scrolling polylines:
 * <ul>
 *   <li>S — steel blue</li>
 *   <li>I — crimson</li>
 *   <li>R — slate gray</li>
 * </ul>
 *
 * <p>A HUD in the top-left corner shows the current basic reproduction number
 * R₀ = β/γ, the epidemic threshold, and the instantaneous S/I/R values.
 * A horizontal dashed line marks R₀ = 1 in the HUD description.
 *
 * <p>The simulation is driven by a {@link Timer} firing every 16 ms.
 * Each frame calls {@link SIROde#step(double, double, double)} once with
 * {@code dt = 0.1}.
 */
public class OdePanel extends JPanel {

    /** Number of time-step snapshots retained in the scrolling history. */
    static final int MAX_HISTORY = 400;

    private static final int    W  = 700;
    private static final int    H  = 480;
    private static final double DT = 0.1;

    // Chart insets
    private static final int LEFT   = 60;
    private static final int RIGHT  = 20;
    private static final int TOP    = 30;
    private static final int BOTTOM = 50;

    private static final Color BG    = new Color(8,  10, 22);
    private static final Color S_COL = new Color(60,  120, 200);
    private static final Color I_COL = new Color(220, 60,  60);
    private static final Color R_COL = new Color(100, 110, 130);
    private static final Color GRID  = new Color(25, 28, 45);
    private static final Color TEXT  = new Color(180, 190, 210);

    private final SIROde model;
    private final Deque<double[]> history = new ArrayDeque<>(MAX_HISTORY + 1);

    double beta  = 0.30;
    double gamma = 0.05;
    boolean paused = false;

    private final Timer gameLoop;

    /**
     * Construct an ODE panel bound to the given model.
     *
     * @param model the {@link SIROde} to visualise
     */
    public OdePanel(SIROde model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);

        // Seed history with initial state
        history.addLast(new double[]{model.getS(), model.getI(), model.getR()});

        gameLoop = new Timer(16, e -> {
            if (!paused) {
                model.step(DT, beta, gamma);
                if (history.size() >= MAX_HISTORY) history.pollFirst();
                history.addLast(new double[]{model.getS(), model.getI(), model.getR()});
            }
            repaint();
        });
        gameLoop.start();
    }

    /** Called by {@link OdeControls} when parameters change. */
    void setBeta(double beta)   { this.beta  = beta; }
    void setGamma(double gamma) { this.gamma = gamma; }

    /** Reset history and model; called by OdeControls. */
    void resetHistory() {
        history.clear();
        history.addLast(new double[]{model.getS(), model.getI(), model.getR()});
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawCurves(g2);
        drawAxes(g2);
        drawHud(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID);
        int chartW = W - LEFT - RIGHT;
        int chartH = H - TOP - BOTTOM;
        for (int row = 0; row <= 5; row++) {
            int y = TOP + row * chartH / 5;
            g2.drawLine(LEFT, y, LEFT + chartW, y);
        }
        for (int col = 0; col <= 8; col++) {
            int x = LEFT + col * chartW / 8;
            g2.drawLine(x, TOP, x, TOP + chartH);
        }
    }

    private void drawCurves(Graphics2D g2) {
        if (history.size() < 2) return;

        double[][] snapshot = history.toArray(new double[0][]);
        int pts   = snapshot.length;
        int chartW = W - LEFT - RIGHT;
        int chartH = H - TOP - BOTTOM;
        double n  = model.getN();

        int[] sxArr = new int[pts], syArr = new int[pts];
        int[] ixArr = new int[pts], iyArr = new int[pts];
        int[] rxArr = new int[pts], ryArr = new int[pts];

        for (int t = 0; t < pts; t++) {
            int x = LEFT + t * chartW / (MAX_HISTORY - 1);
            sxArr[t] = x; syArr[t] = TOP + chartH - (int)(snapshot[t][0] / n * chartH);
            ixArr[t] = x; iyArr[t] = TOP + chartH - (int)(snapshot[t][1] / n * chartH);
            rxArr[t] = x; ryArr[t] = TOP + chartH - (int)(snapshot[t][2] / n * chartH);
        }

        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.setColor(S_COL);
        g2.drawPolyline(sxArr, syArr, pts);

        g2.setColor(I_COL);
        g2.drawPolyline(ixArr, iyArr, pts);

        g2.setColor(R_COL);
        g2.drawPolyline(rxArr, ryArr, pts);

        g2.setStroke(new BasicStroke());
    }

    private void drawAxes(Graphics2D g2) {
        int chartH = H - TOP - BOTTOM;
        double n   = model.getN();

        g2.setColor(new Color(80, 90, 110));
        g2.drawLine(LEFT, TOP, LEFT, TOP + chartH);
        g2.drawLine(LEFT, TOP + chartH, W - RIGHT, TOP + chartH);

        // Y-axis labels
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT);
        for (int tick = 0; tick <= 5; tick++) {
            int y  = TOP + (5 - tick) * chartH / 5;
            int val = (int)(tick * n / 5);
            g2.drawString(String.valueOf(val), 4, y + 4);
            g2.setColor(new Color(50, 55, 70));
            g2.drawLine(LEFT - 4, y, LEFT, y);
            g2.setColor(TEXT);
        }

        // Legend
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        int ly = H - 18;
        int lx = LEFT + 20;
        g2.setColor(S_COL); g2.drawString("S(t)", lx,        ly);
        g2.setColor(I_COL); g2.drawString("I(t)", lx + 60,   ly);
        g2.setColor(R_COL); g2.drawString("R(t)", lx + 120,  ly);
    }

    private void drawHud(Graphics2D g2) {
        double r0 = (gamma > 0) ? beta / gamma : Double.POSITIVE_INFINITY;
        int s  = (int) model.getS();
        int i  = (int) model.getI();
        int r  = (int) model.getR();

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int x = LEFT + 10, y = TOP + 18;

        g2.setColor(TEXT);
        g2.drawString(String.format("R\u2080 = %.2f  (threshold = 1.0)", r0), x, y);

        Color r0Color = (r0 > 1.0) ? new Color(220, 100, 60) : new Color(80, 200, 100);
        g2.setColor(r0Color);
        g2.drawString((r0 > 1.0) ? "Epidemic grows" : "Epidemic declines", x, y + 16);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(S_COL); g2.drawString(String.format("S=%6d", s), W - 130, TOP + 18);
        g2.setColor(I_COL); g2.drawString(String.format("I=%6d", i), W - 130, TOP + 32);
        g2.setColor(R_COL); g2.drawString(String.format("R=%6d", r), W - 130, TOP + 46);

        if (paused) {
            g2.setColor(new Color(255, 200, 60));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("PAUSED", W - 80, 20);
        }
    }
}
