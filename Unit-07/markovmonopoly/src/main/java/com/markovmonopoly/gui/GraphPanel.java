package com.markovmonopoly.gui;

import com.markovmonopoly.core.MarkovChain;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Animated directed-graph visualization of a Markov chain.
 *
 * <h2>What it shows</h2>
 * <ul>
 *   <li>Each state is a labelled circle (node).</li>
 *   <li>Each non-zero transition is a directed arrow whose thickness is
 *       proportional to its probability.</li>
 *   <li>Probability labels appear on significant edges (≥ 0.05).</li>
 *   <li>A bright "walker" dot advances one step per tick, illustrating
 *       the random-walk interpretation of the chain.</li>
 * </ul>
 *
 * <h2>Layout</h2>
 * <ul>
 *   <li>≤ 15 states: circular layout with evenly spaced nodes.</li>
 *   <li>16–30 states: two-column layout (birth-death chains appear as a path).</li>
 *   <li>&gt; 30 states: message shown instead of graph; use Analysis or Board tabs.</li>
 * </ul>
 */
public final class GraphPanel extends JPanel {

    private static final int   NODE_R        = 22;
    private static final int   WALK_INTERVAL = 700;   // ms between walker steps
    private static final Color BG            = new Color(18, 22, 36);
    private static final Color NODE_IDLE     = new Color(55, 75, 130);
    private static final Color NODE_ACTIVE   = new Color(255, 220, 50);
    private static final Color NODE_PREV     = new Color(120, 180, 255);
    private static final Color EDGE_COLOR    = new Color(80, 100, 160);
    private static final Color LABEL_COLOR   = new Color(200, 220, 255);
    private static final Color PROB_COLOR    = new Color(160, 180, 220);

    private final MarkovAppFrame app;

    private MarkovChain chain;
    private int         n;
    private Point[]     pos;       // node screen positions
    private int         current;   // walker's current state index
    private int         previous;  // previous state (highlighted differently)
    private final Random rng = new Random();
    private Timer       walkTimer;

    // ── Controls ──────────────────────────────────────────────────────────────

    private final JToggleButton walkBtn   = new JToggleButton("▶ Walk", false);
    private final JLabel        stateInfo = new JLabel(" ");
    private final JSlider       speedSlider = new JSlider(100, 2000, WALK_INTERVAL);

    // ── Construction ──────────────────────────────────────────────────────────

    public GraphPanel(MarkovAppFrame app) {
        this.app = app;
        setBackground(BG);
        setLayout(new BorderLayout());

        JPanel canvas = new GraphCanvas();
        canvas.setBackground(BG);

        add(canvas,        BorderLayout.CENTER);
        add(buildControls(), BorderLayout.SOUTH);

        walkTimer = new Timer(WALK_INTERVAL, e -> step());
    }

    public void onChainChanged(MarkovChain c) {
        chain   = c;
        n       = c.size();
        current = 0;
        previous = -1;
        computeLayout();
        stateInfo.setText("State: " + chain.getLabel(0));
        repaint();
    }

    // ── Walker ────────────────────────────────────────────────────────────────

    private void step() {
        if (chain == null || n == 0) return;
        previous = current;
        current  = chain.sampleNextState(current, rng);
        stateInfo.setText("State: " + chain.getLabel(current));
        repaint();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private void computeLayout() {
        if (n == 0) { pos = new Point[0]; return; }
        pos = new Point[n];

        Dimension d = getSize();
        int W = Math.max(d.width,  700);
        int H = Math.max(d.height, 520) - 60; // leave room for controls

        if (n <= 15) {
            // Circular layout
            int cx = W / 2, cy = H / 2;
            int r  = Math.min(cx, cy) - NODE_R - 20;
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n - Math.PI / 2;
                pos[i] = new Point(cx + (int)(r * Math.cos(angle)),
                                   cy + (int)(r * Math.sin(angle)));
            }
        } else if (n <= 30) {
            // Two-row layout
            int cols = (n + 1) / 2;
            int hGap = Math.min(70, (W - 60) / cols);
            int row0Y = H / 3, row1Y = 2 * H / 3;
            for (int i = 0; i < n; i++) {
                int col = i / 2;
                int row = i % 2;
                int x = 30 + col * hGap + hGap / 2;
                int y = (row == 0) ? row0Y : row1Y;
                pos[i] = new Point(x, y);
            }
        } else {
            pos = null;  // too large — show message
        }
    }

    // ── Controls ──────────────────────────────────────────────────────────────

    private JPanel buildControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setBackground(new Color(26, 30, 48));

        walkBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        walkBtn.setBackground(new Color(50, 80, 140));
        walkBtn.setForeground(Color.WHITE);
        walkBtn.setFocusPainted(false);
        walkBtn.addActionListener(e -> {
            if (walkBtn.isSelected()) {
                walkTimer.start();
                walkBtn.setText("⏸ Pause");
            } else {
                walkTimer.stop();
                walkBtn.setText("▶ Walk");
            }
        });

        JButton stepBtn = new JButton("Step");
        stepBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        stepBtn.setBackground(new Color(45, 65, 115));
        stepBtn.setForeground(Color.WHITE);
        stepBtn.setFocusPainted(false);
        stepBtn.addActionListener(e -> step());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        resetBtn.setBackground(new Color(80, 55, 35));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.addActionListener(e -> {
            current = 0; previous = -1;
            stateInfo.setText(chain == null ? " " : "State: " + chain.getLabel(0));
            repaint();
        });

        speedSlider.setBackground(new Color(26, 30, 48));
        speedSlider.setForeground(new Color(130, 160, 210));
        speedSlider.setInverted(true);
        speedSlider.setPreferredSize(new Dimension(120, 28));
        speedSlider.addChangeListener(e -> {
            walkTimer.setDelay(speedSlider.getValue());
        });

        JLabel speedLbl = new JLabel("Speed:");
        speedLbl.setForeground(Color.LIGHT_GRAY);
        speedLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

        stateInfo.setForeground(new Color(255, 220, 80));
        stateInfo.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));

        p.add(walkBtn); p.add(stepBtn); p.add(resetBtn);
        p.add(speedLbl); p.add(speedSlider);
        p.add(Box.createHorizontalStrut(20));
        p.add(stateInfo);
        return p;
    }

    // ── Canvas ────────────────────────────────────────────────────────────────

    private class GraphCanvas extends JPanel {
        GraphCanvas() {
            setBackground(BG);
            addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent e) {
                    computeLayout();
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            if (chain == null) return;

            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (pos == null) {
                drawLargeChainMessage(g);
                return;
            }

            drawEdges(g);
            drawNodes(g);
        }

        private void drawLargeChainMessage(Graphics2D g) {
            g.setColor(new Color(120, 140, 180));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String msg = "Graph view is not practical for " + n + " states.";
            String msg2 = "Use the Matrix or Analysis tab to inspect this chain.";
            FontMetrics fm = g.getFontMetrics();
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g.drawString(msg,  cx - fm.stringWidth(msg)  / 2, cy - 10);
            g.drawString(msg2, cx - fm.stringWidth(msg2) / 2, cy + 14);
        }

        // ── Edges ─────────────────────────────────────────────────────────────

        private void drawEdges(Graphics2D g) {
            double[][] mat = chain.getMatrix().toArray();

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double p = mat[i][j];
                    if (p < 0.005) continue;

                    if (i == j) {
                        drawSelfLoop(g, i, p);
                    } else {
                        boolean reverse = mat[j][i] > 0.005;
                        drawArrow(g, i, j, p, reverse);
                    }
                }
            }
        }

        private void drawArrow(Graphics2D g, int from, int to, double prob, boolean bidirectional) {
            Point a = pos[from], b = pos[to];
            float alpha = (float) Math.min(1.0, 0.25 + prob * 1.5);
            g.setColor(alphaColor(EDGE_COLOR, alpha));
            float stroke = (float) Math.max(0.6, prob * 5.0);
            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            double dx = b.x - a.x, dy = b.y - a.y;
            double len = Math.sqrt(dx * dx + dy * dy);
            double ux = dx / len, uy = dy / len;

            // Offset for bidirectional edges
            double offset = bidirectional ? 7.0 : 0.0;
            double nx = -uy * offset, ny = ux * offset;

            double sx = a.x + ux * NODE_R + nx;
            double sy = a.y + uy * NODE_R + ny;
            double ex = b.x - ux * NODE_R + nx;
            double ey = b.y - uy * NODE_R + ny;

            // Slight curve for readability
            double curveX = (sx + ex) / 2 + nx * 1.5;
            double curveY = (sy + ey) / 2 + ny * 1.5;
            QuadCurve2D curve = new QuadCurve2D.Double(sx, sy, curveX, curveY, ex, ey);
            g.draw(curve);

            // Arrowhead
            drawArrowHead(g, ex, ey, curveX, curveY);

            // Probability label for significant edges
            if (prob >= 0.05) {
                double lx = (sx + ex) / 2 + nx * 0.5;
                double ly = (sy + ey) / 2 + ny * 0.5;
                g.setColor(PROB_COLOR);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
                String label = String.format("%.2f", prob);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(label, (int)(lx - fm.stringWidth(label) / 2.0), (int)(ly + fm.getAscent() / 2.0));
            }

            g.setStroke(new BasicStroke(1.0f));
        }

        private void drawArrowHead(Graphics2D g, double tx, double ty, double fromX, double fromY) {
            double dx = tx - fromX, dy = ty - fromY;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len < 1e-6) return;
            double ux = dx / len, uy = dy / len;
            double size = 7.0;
            double px = -uy * size * 0.5, py = ux * size * 0.5;
            int[] xp = { (int)(tx), (int)(tx - ux * size + px), (int)(tx - ux * size - px) };
            int[] yp = { (int)(ty), (int)(ty - uy * size + py), (int)(ty - uy * size - py) };
            g.fillPolygon(xp, yp, 3);
        }

        private void drawSelfLoop(Graphics2D g, int idx, double prob) {
            Point c = pos[idx];
            float alpha = (float) Math.min(1.0, 0.3 + prob * 1.2);
            g.setColor(alphaColor(EDGE_COLOR, alpha));
            float stroke = (float) Math.max(0.6, prob * 4.0);
            g.setStroke(new BasicStroke(stroke));
            int r = NODE_R + 6;
            g.drawOval(c.x - r / 2, c.y - NODE_R - r, r, r);
            if (prob >= 0.05) {
                g.setColor(PROB_COLOR);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
                String label = String.format("%.2f", prob);
                g.drawString(label, c.x - r / 2, c.y - NODE_R - r - 2);
            }
            g.setStroke(new BasicStroke(1.0f));
        }

        // ── Nodes ─────────────────────────────────────────────────────────────

        private void drawNodes(Graphics2D g) {
            for (int i = 0; i < n; i++) {
                Point c = pos[i];
                boolean active   = (i == current);
                boolean wasPrev  = (i == previous);

                // Node fill
                Color fill = active ? NODE_ACTIVE : (wasPrev ? NODE_PREV : NODE_IDLE);
                if (active) {
                    RadialGradientPaint glow = new RadialGradientPaint(
                        new Point2D.Float(c.x, c.y), NODE_R * 2.5f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(255, 220, 50, 80), new Color(255, 220, 50, 0)});
                    g.setPaint(glow);
                    g.fillOval(c.x - NODE_R * 2, c.y - NODE_R * 2, NODE_R * 4, NODE_R * 4);
                }

                g.setColor(fill);
                g.fillOval(c.x - NODE_R, c.y - NODE_R, NODE_R * 2, NODE_R * 2);
                g.setColor(active ? new Color(255, 255, 180) : new Color(100, 130, 200));
                g.setStroke(new BasicStroke(active ? 2.5f : 1.0f));
                g.drawOval(c.x - NODE_R, c.y - NODE_R, NODE_R * 2, NODE_R * 2);
                g.setStroke(new BasicStroke(1.0f));

                // Label
                String label = chain.getLabel(i);
                if (label.length() > 8) label = label.substring(0, 7) + "…";
                g.setColor(active ? Color.BLACK : LABEL_COLOR);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(8, 10 - n / 5)));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(label, c.x - fm.stringWidth(label) / 2, c.y + fm.getAscent() / 2 - 1);
            }
        }
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private static Color alphaColor(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
    }
}
