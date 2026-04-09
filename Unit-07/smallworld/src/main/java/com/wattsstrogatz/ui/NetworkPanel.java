package com.wattsstrogatz.ui;

import com.wattsstrogatz.model.Edge;
import com.wattsstrogatz.model.Network;
import com.wattsstrogatz.model.NetworkMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Objects;

/**
 * Swing panel that renders a Watts-Strogatz network as a ring diagram.
 *
 * <p>Nodes are placed uniformly on a circle. Edges are colour-coded:
 * <ul>
 *   <li>Original lattice edges — medium grey</li>
 *   <li>Rewired edges — vivid orange-red, drawn slightly thicker</li>
 *   <li>Last-visited edge — yellow/gold glow, drawn on top during animation</li>
 * </ul>
 * Hovering over a node highlights it and shows an info box (index, degree,
 * local clustering coefficient).  Nodes are rendered on top of all edges.
 */
public final class NetworkPanel extends JPanel {

    private static final Color BACKGROUND      = new Color(245, 245, 248);
    private static final Color NODE_FILL       = new Color(50, 100, 160);
    private static final Color NODE_BORDER     = new Color(255, 255, 255);
    private static final Color NODE_HOVER_FILL = new Color(255, 185, 50);
    private static final Color LATTICE_EDGE    = new Color(160, 170, 185);
    private static final Color REWIRED_EDGE    = new Color(220, 80, 50);
    private static final Color HIGHLIGHT_EDGE  = new Color(255, 215, 30);
    private static final float LATTICE_STROKE  = 1.0f;
    private static final float REWIRED_STROKE  = 1.8f;
    private static final float HIGHLIGHT_STROKE = 3.2f;
    private static final int   NODE_DIAM       = 8;
    private static final int   NODE_HOVER_DIAM = 13;
    private static final int   HIT_RADIUS_SQ   = 16 * 16; // pixels²

    private Network network;
    private Edge    highlightEdge;   // last-visited edge; null before first step

    // Cached screen positions — resized whenever nodeCount changes
    private double[] cx = new double[0];
    private double[] cy = new double[0];

    // Hover state
    private int hoveredNode = -1;
    private int mouseX, mouseY;

    /** Creates an empty network panel. */
    public NetworkPanel() {
        setBackground(BACKGROUND);
        setPreferredSize(new Dimension(560, 560));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                updateHoveredNode();
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoveredNode = -1;
                repaint();
            }
        });
    }

    /**
     * Updates the network reference and repaints.
     *
     * @param network the network to render; must not be null
     */
    public void setNetwork(Network network) {
        Objects.requireNonNull(network, "network must not be null");
        this.network = network;
        int n = network.getNodeCount();
        if (cx.length != n) { cx = new double[n]; cy = new double[n]; hoveredNode = -1; }
        repaint();
    }

    /**
     * Sets the edge to highlight (last visited during animation).
     * Does not repaint — caller should call {@link #setNetwork} or repaint separately.
     *
     * @param edge the edge to highlight, or null to clear
     */
    public void setHighlightEdge(Edge edge) {
        this.highlightEdge = edge;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (network == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int    n       = network.getNodeCount();
            int    margin  = NODE_DIAM + 14;
            double centreX = getWidth()  / 2.0;
            double centreY = getHeight() / 2.0;
            double radius  = Math.min(centreX, centreY) - margin;

            for (int i = 0; i < n; i++) {
                double angle = 2.0 * Math.PI * i / n - Math.PI / 2.0;
                cx[i] = centreX + radius * Math.cos(angle);
                cy[i] = centreY + radius * Math.sin(angle);
            }

            // --- Lattice edges (behind rewired and highlight) ---
            g2.setStroke(new BasicStroke(LATTICE_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(LATTICE_EDGE);
            for (Edge e : network.getEdges())
                if (!e.isRewired())
                    g2.draw(new Line2D.Double(cx[e.getU()], cy[e.getU()], cx[e.getV()], cy[e.getV()]));

            // --- Rewired edges ---
            g2.setStroke(new BasicStroke(REWIRED_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(REWIRED_EDGE);
            for (Edge e : network.getEdges())
                if (e.isRewired())
                    g2.draw(new Line2D.Double(cx[e.getU()], cy[e.getU()], cx[e.getV()], cy[e.getV()]));

            // --- Highlight edge: gold glow passes + solid line on top ---
            if (highlightEdge != null) {
                int hu = highlightEdge.getU(), hv = highlightEdge.getV();
                if (hu < n && hv < n) {
                    for (int pass = 3; pass >= 1; pass--) {
                        g2.setColor(new Color(1f, 0.84f, 0.1f, 0.22f * pass));
                        g2.setStroke(new BasicStroke(HIGHLIGHT_STROKE + pass * 3.5f,
                            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.draw(new Line2D.Double(cx[hu], cy[hu], cx[hv], cy[hv]));
                    }
                    g2.setStroke(new BasicStroke(HIGHLIGHT_STROKE,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(HIGHLIGHT_EDGE);
                    g2.draw(new Line2D.Double(cx[hu], cy[hu], cx[hv], cy[hv]));
                }
            }

            // --- Nodes (on top of all edges) ---
            for (int i = 0; i < n; i++) {
                boolean hovered = (i == hoveredNode);
                int diam = hovered ? NODE_HOVER_DIAM : NODE_DIAM;
                int r    = diam / 2;
                int x    = (int) Math.round(cx[i]) - r;
                int y    = (int) Math.round(cy[i]) - r;
                g2.setColor(hovered ? NODE_HOVER_FILL : NODE_FILL);
                g2.fillOval(x, y, diam, diam);
                g2.setStroke(new BasicStroke(1.0f));
                g2.setColor(NODE_BORDER);
                g2.drawOval(x, y, diam, diam);
            }

            // --- Hover info box ---
            if (hoveredNode >= 0) paintHoverBox(g2, n);

        } finally {
            g2.dispose();
        }
    }

    private void paintHoverBox(Graphics2D g2, int n) {
        int node = hoveredNode;
        int deg  = network.degree(node);
        double cc = NetworkMetrics.localClusteringCoefficient(network, node);
        String ccStr = cc < 0 ? "n/a" : String.format("%.3f", cc);

        String line1 = "Node " + node;
        String line2 = "Degree:  " + deg;
        String line3 = "CC:  " + ccStr;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight();
        int boxW  = Math.max(fm.stringWidth(line2), fm.stringWidth(line3)) + 16;
        int boxH  = lineH * 3 + 10;

        // Position box: prefer right-down from node, clamp to panel
        int bx = (int) Math.round(cx[node]) + 12;
        int by = (int) Math.round(cy[node]) - boxH / 2;
        if (bx + boxW > getWidth()  - 4) bx = (int) Math.round(cx[node]) - boxW - 12;
        if (by < 4)                       by = 4;
        if (by + boxH > getHeight() - 4)  by = getHeight() - boxH - 4;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(bx + 2, by + 2, boxW, boxH, 6, 6);

        // Background + border
        g2.setColor(new Color(255, 255, 240, 235));
        g2.fillRoundRect(bx, by, boxW, boxH, 6, 6);
        g2.setColor(new Color(180, 175, 140));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(bx, by, boxW, boxH, 6, 6);

        // Text
        g2.setColor(new Color(40, 40, 50));
        int tx = bx + 8;
        int ty = by + lineH;
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString(line1, tx, ty);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString(line2, tx, ty + lineH);
        g2.drawString(line3, tx, ty + lineH * 2);
    }

    private void updateHoveredNode() {
        if (network == null || cx.length == 0) { hoveredNode = -1; return; }
        int    best  = -1;
        double bestD = HIT_RADIUS_SQ;
        for (int i = 0; i < cx.length; i++) {
            double dx = mouseX - cx[i], dy = mouseY - cy[i];
            double d2 = dx * dx + dy * dy;
            if (d2 < bestD) { bestD = d2; best = i; }
        }
        hoveredNode = best;
    }
}
