package com.markovmonopoly.gui;

import com.markovmonopoly.core.MarkovAnalysis;
import com.markovmonopoly.core.MarkovChain;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Monopoly board heat-map panel.
 *
 * <p>Draws the classic Monopoly board layout (40 spaces arranged around a square)
 * with each space coloured by its stationary-distribution visit probability.
 * Cooler colours (blue-green) = rarely visited; warmer colours (orange-red) = frequently
 * visited. The "In Jail" virtual state (index 40) is shown in the center.
 *
 * <p>This tab is only enabled when the Monopoly chain is loaded.
 *
 * <h2>Board layout (standard Monopoly)</h2>
 * <pre>
 *   Bottom row  (L→R): spaces  0–10
 *   Right column (B→T): spaces 11–19  [displayed as 10 down the left actually...]
 *   Actually standard layout:
 *   Bottom row  (L→R):  0–10    (11 spaces including corners)
 *   Left column (B→T):  11–19   (mapped to right side visually: 11–19 bottom→top)
 *   Top row     (R→L): 20–30    (11 spaces)
 *   Right col   (T→B): 31–39   (mapped to left side visually)
 * </pre>
 *
 * <p>Space 30 (Go To Jail) will show essentially zero probability since the chain
 * always immediately moves the player to the IN_JAIL state.
 */
public final class MonopolyBoardPanel extends JPanel {

    private static final Color BG = new Color(20, 25, 40);

    // Board-space colours by property group (matching standard Monopoly palette)
    private static final Color[] GROUP_COLORS = {
        new Color(150, 75,  0),   // Purple  / Brown
        new Color(170, 224, 250), // Light Blue
        new Color(216, 0,   115), // Pink / Magenta
        new Color(253, 147, 0),   // Orange
        new Color(237, 28,  36),  // Red
        new Color(255, 239, 0),   // Yellow
        new Color(31,  178, 90),  // Green
        new Color(0,   70,  127), // Dark Blue
    };

    private double[]    pi;          // stationary distribution (41 values)
    private MarkovChain chain;
    private int         hoveredIdx = -1;

    private static final String[] SHORT_NAMES = {
        "GO","Med","Comm","Balt","Inc Tax","Read RR","Orient","Chance","Vermont","Conn",
        "Jail","St.Chas","Elec","States","Virginia","Penn RR","St.James","Comm","Tenn","New York",
        "Free Park","Kentucky","Chance","Indiana","Illinois","B&O RR","Atlantic","Ventnor","Water","Marvin",
        "GoJail","Pacific","N.Carol","Comm","Penn Ave","Short RR","Chance","Park Pl","Lux Tax","Boardwalk",
        "In Jail"
    };

    // ── Construction ──────────────────────────────────────────────────────────

    public MonopolyBoardPanel() {
        setBackground(BG);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(700, 700));

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                hoveredIdx = spaceAt(e.getX(), e.getY());
                repaint();
            }
        });

        add(buildLegend(), BorderLayout.SOUTH);
    }

    public void onChainChanged(MarkovChain c) {
        chain = c;
        if (c == null) { pi = null; repaint(); return; }
        try {
            pi = MarkovAnalysis.stationaryDistribution(c);
        } catch (Exception ex) {
            pi = null;
        }
        repaint();
    }

    // ── Paint ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (pi == null) {
            g.setColor(new Color(90, 110, 160));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            String msg = "Select the 'Monopoly (Theoretical)' chain to view the board heat-map.";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg, getWidth() / 2 - fm.stringWidth(msg) / 2, getHeight() / 2);
            return;
        }

        int W = getWidth(), H = getHeight() - 40; // subtract legend strip
        int boardSize = Math.min(W, H) - 20;
        int originX = (W - boardSize) / 2;
        int originY = (H - boardSize) / 2;

        // Divide into 11×11 grid; corner cells are 1.5× larger
        // Standard Monopoly layout:
        // 11 spaces per side including corners → each non-corner cell width = boardSize/11
        int cells = 11;
        int cellW = boardSize / cells;
        int corner = cellW; // same size for simplicity

        // ── Draw 40 board spaces + center ────────────────────────────────────
        drawBoardSpaces(g, originX, originY, boardSize, cellW);

        // ── Title ─────────────────────────────────────────────────────────────
        g.setColor(new Color(200, 220, 255));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.drawString("Monopoly Visit-Frequency Heat-Map", originX, originY - 6);

        // ── Center label ──────────────────────────────────────────────────────
        int cx = originX + cellW;
        int cy = originY + cellW;
        int cw = boardSize - 2 * cellW;
        g.setColor(new Color(30, 40, 65));
        g.fillRect(cx, cy, cw, cw);
        g.setColor(new Color(50, 80, 130));
        g.drawRect(cx, cy, cw, cw);

        g.setColor(new Color(120, 150, 210));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        String[] centerLines = {"MONOPOLY", "MARKOV CHAIN", "HEAT-MAP"};
        int lineH = 18;
        int startY = cy + cw / 2 - (centerLines.length * lineH) / 2 + lineH;
        for (String line : centerLines) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(line, cx + (cw - fm.stringWidth(line)) / 2, startY);
            startY += lineH;
        }

        // ── In Jail label (index 40) in center ───────────────────────────────
        if (chain != null && chain.size() == 41) {
            drawInJailIndicator(g, cx, cy, cw);
        }

        // ── Hovered tooltip ───────────────────────────────────────────────────
        if (hoveredIdx >= 0 && chain != null && hoveredIdx < pi.length) {
            drawTooltip(g, hoveredIdx);
        }
    }

    // ── Board space rendering ─────────────────────────────────────────────────

    private void drawBoardSpaces(Graphics2D g, int ox, int oy, int board, int cw) {
        // Bottom row: spaces 0–10 (right→left in standard board, but we draw left→right)
        // Traditionally: corner 10 at bottom-left, corner 0 (GO) at bottom-right
        // We'll follow the standard: bottom row goes from right (GO=0) to left (Jail=10)
        for (int i = 0; i <= 10; i++) {
            int spaceIdx = i;                       // 0=GO, 10=Jail/Just Visiting
            int x = ox + (10 - i) * cw;
            int y = oy + board - cw;
            drawSpace(g, spaceIdx, x, y, cw, cw, false);
        }
        // Left column: spaces 11–19 (bottom→top)
        for (int i = 0; i < 9; i++) {
            int spaceIdx = 11 + i;
            int x = ox;
            int y = oy + board - 2 * cw - i * cw;
            drawSpace(g, spaceIdx, x, y, cw, cw, false);
        }
        // Top row: spaces 20–30 (left→right)
        for (int i = 0; i <= 10; i++) {
            int spaceIdx = 20 + i;
            int x = ox + i * cw;
            int y = oy;
            drawSpace(g, spaceIdx, x, y, cw, cw, false);
        }
        // Right column: spaces 31–39 (top→bottom)
        for (int i = 0; i < 9; i++) {
            int spaceIdx = 31 + i;
            int x = ox + board - cw;
            int y = oy + cw + i * cw;
            drawSpace(g, spaceIdx, x, y, cw, cw, false);
        }
    }

    private void drawSpace(Graphics2D g, int spaceIdx, int x, int y, int w, int h,
                            boolean rotated) {
        double prob = (pi != null && spaceIdx < pi.length) ? pi[spaceIdx] : 0;
        Color heat = heatColor(prob);

        boolean hovered = (spaceIdx == hoveredIdx);
        if (hovered) heat = heat.brighter();

        // Fill
        g.setColor(heat);
        g.fillRect(x, y, w, h);

        // Border
        g.setColor(hovered ? Color.WHITE : new Color(50, 70, 120));
        g.setStroke(new BasicStroke(hovered ? 2f : 0.8f));
        g.drawRect(x, y, w, h);
        g.setStroke(new BasicStroke(1f));

        // Short name label
        g.setColor(labelColor(heat));
        int fontSize = Math.max(6, Math.min(9, w / 7));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        String name = SHORT_NAMES[spaceIdx];
        FontMetrics fm = g.getFontMetrics();
        // Wrap to two lines if needed
        int half = name.length() / 2;
        String line1 = name.substring(0, half).trim();
        String line2 = name.substring(half).trim();
        g.drawString(line1, x + (w - fm.stringWidth(line1)) / 2, y + h / 2 - 2);
        g.drawString(line2, x + (w - fm.stringWidth(line2)) / 2, y + h / 2 + fontSize + 1);

        // Probability bar at bottom of cell
        if (prob > 0) {
            double maxPi = 0;
            if (pi != null) for (double v : pi) maxPi = Math.max(maxPi, v);
            int barH = Math.max(2, (int)(h * 0.18 * prob / Math.max(maxPi, 1e-12)));
            g.setColor(new Color(255, 255, 255, 80));
            g.fillRect(x + 1, y + h - barH - 1, w - 2, barH);
        }
    }

    private void drawInJailIndicator(Graphics2D g, int cx, int cy, int cw) {
        double prob = pi[40];
        Color heat  = heatColor(prob);

        int iw = cw / 4, ih = cw / 6;
        int ix = cx + cw / 2 - iw / 2;
        int iy = cy + cw - ih - 12;

        g.setColor(heat);
        g.fill(new RoundRectangle2D.Float(ix, iy, iw, ih, 6, 6));
        g.setColor(new Color(180, 200, 240));
        g.draw(new RoundRectangle2D.Float(ix, iy, iw, ih, 6, 6));

        g.setColor(labelColor(heat));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        FontMetrics fm = g.getFontMetrics();
        String l1 = "In Jail";
        String l2 = String.format("%.3f", prob);
        g.drawString(l1, ix + (iw - fm.stringWidth(l1)) / 2, iy + ih / 2 - 1);
        g.drawString(l2, ix + (iw - fm.stringWidth(l2)) / 2, iy + ih / 2 + 10);
    }

    private void drawTooltip(Graphics2D g, int idx) {
        String name = chain.getLabel(idx);
        String prob = String.format("π = %.5f  (%.2f%% of visits)", pi[idx], pi[idx] * 100);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int tw = Math.max(fm.stringWidth(name), fm.stringWidth(prob)) + 20;
        int th = 36;
        int tx = 10, ty = getHeight() - 50 - th;

        g.setColor(new Color(30, 40, 65, 220));
        g.fillRoundRect(tx, ty, tw, th, 8, 8);
        g.setColor(new Color(100, 140, 220));
        g.drawRoundRect(tx, ty, tw, th, 8, 8);
        g.setColor(new Color(220, 230, 255));
        g.drawString(name, tx + 10, ty + 14);
        g.setColor(new Color(255, 220, 80));
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        g.drawString(prob, tx + 10, ty + 28);
    }

    // ── Heat colour mapping ───────────────────────────────────────────────────

    /**
     * Maps a probability value to a heat colour from cool (low) to warm (high).
     * Uses a 3-stop gradient: blue → yellow → red.
     */
    private Color heatColor(double prob) {
        if (pi == null || prob <= 0) return new Color(28, 36, 60);
        double maxPi = 0;
        for (double v : pi) maxPi = Math.max(maxPi, v);
        double t = maxPi > 0 ? Math.min(1.0, prob / maxPi) : 0;

        // blue (0,80,200) → cyan (0,200,200) → yellow (240,220,0) → red (220,50,0)
        if (t < 0.33) {
            double s = t / 0.33;
            return blend(new Color(0, 80, 200), new Color(0, 190, 180), s);
        } else if (t < 0.66) {
            double s = (t - 0.33) / 0.33;
            return blend(new Color(0, 190, 180), new Color(240, 210, 0), s);
        } else {
            double s = (t - 0.66) / 0.34;
            return blend(new Color(240, 210, 0), new Color(220, 50, 20), s);
        }
    }

    private static Color blend(Color a, Color b, double t) {
        int r = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int gv= (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl= (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        return new Color(Math.max(0,Math.min(255,r)), Math.max(0,Math.min(255,gv)), Math.max(0,Math.min(255,bl)));
    }

    private static Color labelColor(Color bg) {
        double luminance = 0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue();
        return luminance > 128 ? Color.BLACK : Color.WHITE;
    }

    // ── Hit testing ───────────────────────────────────────────────────────────

    private int spaceAt(int mx, int my) {
        if (pi == null) return -1;
        int W = getWidth(), H = getHeight() - 40;
        int boardSize = Math.min(W, H) - 20;
        int ox = (W - boardSize) / 2;
        int oy = (H - boardSize) / 2;
        int cw = boardSize / 11;

        // Bottom row
        for (int i = 0; i <= 10; i++) {
            int x = ox + (10 - i) * cw, y = oy + boardSize - cw;
            if (mx >= x && mx < x + cw && my >= y && my < y + cw) return i;
        }
        // Left column
        for (int i = 0; i < 9; i++) {
            int x = ox, y = oy + boardSize - 2 * cw - i * cw;
            if (mx >= x && mx < x + cw && my >= y && my < y + cw) return 11 + i;
        }
        // Top row
        for (int i = 0; i <= 10; i++) {
            int x = ox + i * cw, y = oy;
            if (mx >= x && mx < x + cw && my >= y && my < y + cw) return 20 + i;
        }
        // Right column
        for (int i = 0; i < 9; i++) {
            int x = ox + boardSize - cw, y = oy + cw + i * cw;
            if (mx >= x && mx < x + cw && my >= y && my < y + cw) return 31 + i;
        }
        // Center: In Jail (state 40)
        int cx = ox + cw, cy = oy + cw, csize = boardSize - 2 * cw;
        if (mx >= cx && mx < cx + csize && my >= cy && my < cy + csize) return 40;
        return -1;
    }

    // ── Legend ────────────────────────────────────────────────────────────────

    private JPanel buildLegend() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                int W = getWidth(), H = getHeight();
                // Gradient bar
                int barX = 80, barY = 8, barW = W - 160, barH = 16;
                for (int x = 0; x < barW; x++) {
                    double t = (double) x / barW;
                    Color c = (t < 0.33) ? blend(new Color(0,80,200), new Color(0,190,180), t/0.33)
                            : (t < 0.66) ? blend(new Color(0,190,180), new Color(240,210,0), (t-0.33)/0.33)
                            :              blend(new Color(240,210,0), new Color(220,50,20), (t-0.66)/0.34);
                    g.setColor(c);
                    g.fillRect(barX + x, barY, 1, barH);
                }
                g.setColor(new Color(80, 100, 160));
                g.drawRect(barX, barY, barW, barH);
                g.setColor(new Color(150, 170, 210));
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                g.drawString("Less visited", barX - 78, barY + 12);
                g.drawString("More visited", barX + barW + 4, barY + 12);
            }
        };
        p.setBackground(new Color(26, 32, 52));
        p.setPreferredSize(new Dimension(0, 36));
        return p;
    }
}
