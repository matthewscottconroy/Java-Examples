package com.epidemic.ui;

import com.epidemic.model.NodeState;
import com.epidemic.model.SIRNetwork;
import com.epidemic.model.WattsStrogatz;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Simulation canvas for the agent-based Network SIR model.
 *
 * <p>Nodes are arranged in a circular layout at radius 200 from the panel
 * centre.  Edges are drawn as thin gray lines; nodes are filled circles coloured
 * by their current {@link NodeState}:
 * <ul>
 *   <li>Susceptible — steel blue {@code (60, 120, 200)}</li>
 *   <li>Infected    — crimson    {@code (220, 60, 60)}</li>
 *   <li>Recovered   — slate gray {@code (100, 110, 130)}</li>
 * </ul>
 *
 * <p>A heads-up display (HUD) in the top-left corner shows current S/I/R counts.
 * A proportional bar chart at the bottom of the canvas illustrates the
 * S/I/R split at a glance.
 *
 * <p>The simulation is driven by a {@link Timer} firing every 16 ms (~60 fps).
 * Each frame advances the model by {@link #stepsPerFrame} steps.
 */
public class NetworkPanel extends JPanel {

    private static final int W      = 700;
    private static final int H      = 480;
    private static final int RADIUS = 200;

    private static final Color BG    = new Color(8,  10, 22);
    private static final Color EDGE  = new Color(50, 55, 70);
    private static final Color S_COL = new Color(60,  120, 200);
    private static final Color I_COL = new Color(220, 60,  60);
    private static final Color R_COL = new Color(100, 110, 130);
    private static final Color TEXT  = new Color(180, 190, 210);

    private SIRNetwork model;
    private double     beta          = 0.3;
    private double     gamma         = 0.05;
    int                stepsPerFrame = 1;
    boolean            paused        = false;

    private final Random rng      = new Random();
    private final Timer  gameLoop;

    /**
     * Construct a network panel bound to the given model.
     *
     * @param model the {@link SIRNetwork} to visualise
     */
    public NetworkPanel(SIRNetwork model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);

        gameLoop = new Timer(16, e -> {
            if (!paused && !model.isEpidemicOver()) {
                for (int s = 0; s < stepsPerFrame; s++) {
                    model.step(beta, gamma, rng);
                }
            }
            repaint();
        });
        gameLoop.start();
    }

    // -------------------------------------------------------------------------
    // Model / parameter setters (called from NetworkControls)
    // -------------------------------------------------------------------------

    /** Replace the model entirely (triggered by Rebuild). */
    void setModel(SIRNetwork model) {
        this.model = model;
    }

    void setBeta(double beta)   { this.beta  = beta; }
    void setGamma(double gamma) { this.gamma = gamma; }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawEdges(g2);
        drawNodes(g2);
        drawHud(g2);
        drawBarChart(g2);
    }

    private void drawEdges(Graphics2D g2) {
        WattsStrogatz graph = model.getGraph();
        int n = graph.nodeCount();
        g2.setColor(EDGE);
        g2.setStroke(new BasicStroke(0.8f));

        int cx = W / 2;
        int cy = H / 2 - 40;

        for (int i = 0; i < n; i++) {
            int xi = cx + (int)(RADIUS * Math.cos(2 * Math.PI * i / n));
            int yi = cy + (int)(RADIUS * Math.sin(2 * Math.PI * i / n));

            for (int j : graph.neighbors(i)) {
                if (j > i) {   // draw each edge once
                    int xj = cx + (int)(RADIUS * Math.cos(2 * Math.PI * j / n));
                    int yj = cy + (int)(RADIUS * Math.sin(2 * Math.PI * j / n));
                    g2.drawLine(xi, yi, xj, yj);
                }
            }
        }
        g2.setStroke(new BasicStroke());
    }

    private void drawNodes(Graphics2D g2) {
        WattsStrogatz graph = model.getGraph();
        int n = graph.nodeCount();
        int nodeR = Math.max(4, 12 - n / 30);

        int cx = W / 2;
        int cy = H / 2 - 40;

        for (int i = 0; i < n; i++) {
            int x = cx + (int)(RADIUS * Math.cos(2 * Math.PI * i / n));
            int y = cy + (int)(RADIUS * Math.sin(2 * Math.PI * i / n));

            Color fill = switch (model.getState(i)) {
                case SUSCEPTIBLE -> S_COL;
                case INFECTED    -> I_COL;
                case RECOVERED   -> R_COL;
            };
            g2.setColor(fill);
            g2.fillOval(x - nodeR, y - nodeR, nodeR * 2, nodeR * 2);
            g2.setColor(fill.darker());
            g2.drawOval(x - nodeR, y - nodeR, nodeR * 2, nodeR * 2);
        }
    }

    private void drawHud(Graphics2D g2) {
        int s = model.getSusceptible();
        int i = model.getInfected();
        int r = model.getRecovered();

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int x = 12, y = 20;
        g2.setColor(S_COL);
        g2.drawString(String.format("S = %4d", s), x, y);
        g2.setColor(I_COL);
        g2.drawString(String.format("I = %4d", i), x, y + 16);
        g2.setColor(R_COL);
        g2.drawString(String.format("R = %4d", r), x, y + 32);

        if (model.isEpidemicOver()) {
            g2.setColor(new Color(100, 200, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("Epidemic Over", W - 140, 20);
        }
        if (paused) {
            g2.setColor(new Color(255, 200, 60));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("PAUSED", W - 80, 38);
        }
    }

    private void drawBarChart(Graphics2D g2) {
        int n   = model.getN();
        int s   = model.getSusceptible();
        int i   = model.getInfected();
        int r   = model.getRecovered();

        int barY  = H - 28;
        int barH  = 18;
        int barW  = W - 80;
        int barX  = 40;

        // Background
        g2.setColor(new Color(20, 25, 40));
        g2.fillRect(barX, barY, barW, barH);

        // S segment
        int sw = (int)((double) s / n * barW);
        g2.setColor(S_COL);
        g2.fillRect(barX, barY, sw, barH);

        // I segment
        int iw = (int)((double) i / n * barW);
        g2.setColor(I_COL);
        g2.fillRect(barX + sw, barY, iw, barH);

        // R segment
        int rw = barW - sw - iw;
        g2.setColor(R_COL);
        g2.fillRect(barX + sw + iw, barY, rw, barH);

        // Border
        g2.setColor(new Color(60, 70, 90));
        g2.drawRect(barX, barY, barW, barH);

        // Labels
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(TEXT);
        g2.drawString("S", barX - 12, barY + barH - 4);
        g2.drawString("R", barX + barW + 4, barY + barH - 4);
    }
}
