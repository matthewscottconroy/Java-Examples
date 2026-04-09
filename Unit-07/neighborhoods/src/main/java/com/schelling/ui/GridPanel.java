package com.schelling.ui;

import com.schelling.model.AgentType;
import com.schelling.model.Grid;
import com.schelling.model.NeighborhoodType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * A Swing panel that renders the Schelling simulation grid.
 *
 * <h2>View modes</h2>
 * <ul>
 *   <li><b>Normal</b> — cells colored by agent type (palette-aware)</li>
 *   <li><b>Heatmap</b> — cells colored red→amber→green by satisfaction ratio;
 *       an overlay on top of the type color so empty cells stay grey</li>
 *   <li><b>Highlight unsatisfied</b> — draws an amber border around every
 *       unsatisfied agent cell</li>
 * </ul>
 *
 * <h2>Hover inspection</h2>
 * Hovering over a cell highlights its full neighborhood and shows an info box
 * with agent type, satisfaction ratio, satisfied status, and neighbour counts.
 *
 * <h2>Paint mode</h2>
 * When a {@link PaintMode} other than {@code OFF} is active, clicking or
 * dragging on the grid places / erases agents and fires
 * {@link GridCellListener#onCellPainted} for each affected cell.
 */
public final class GridPanel extends JPanel {

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum PaintMode  { OFF, PAINT_A, PAINT_B, ERASE }
    public enum ColorPalette { DEFAULT, COLORBLIND }

    /** Callback fired when the user paints a cell (paint mode active). */
    public interface GridCellListener {
        /** @param type the new agent type, or {@code null} for erase */
        void onCellPainted(int row, int col, AgentType type);
    }

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final Color EMPTY_COLOR     = new Color(38, 42, 58);
    private static final Color BG_COLOR        = new Color(18, 20, 32);
    private static final Color GRID_LINE_COLOR = new Color(30, 34, 50);
    private static final int   MIN_CELL_SIZE   = 2;

    // Colorblind palette (Wong 2011 — distinguishable for most forms of CVD)
    private static final Color CB_TYPE_A = new Color(  0, 114, 178);  // blue
    private static final Color CB_TYPE_B = new Color(230, 159,   0);  // orange

    // ── State ─────────────────────────────────────────────────────────────────

    private Grid             grid;
    private boolean          showHeatmap           = false;
    private boolean          highlightUnsatisfied  = false;
    private PaintMode        paintMode             = PaintMode.OFF;
    private ColorPalette     palette               = ColorPalette.DEFAULT;
    private NeighborhoodType neighborhoodType      = NeighborhoodType.MOORE;
    private double           thresholdA            = 0.33;
    private double           thresholdB            = 0.33;
    private int              hoverRow              = -1;
    private int              hoverCol              = -1;
    private GridCellListener cellListener;

    // ── Constructor ───────────────────────────────────────────────────────────

    public GridPanel() {
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(650, 650));

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e)   { updateHover(e); repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hoverRow = hoverCol = -1; repaint(); }
            @Override public void mousePressed(MouseEvent e) { handlePaint(e); }
            @Override public void mouseDragged(MouseEvent e) { handlePaint(e); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // ── Public setters ────────────────────────────────────────────────────────

    public void setGrid(Grid grid) {
        if (grid == null) throw new NullPointerException("grid must not be null");
        this.grid = grid;
    }

    public void setShowHeatmap(boolean b)          { showHeatmap = b; }
    public void setHighlightUnsatisfied(boolean b) { highlightUnsatisfied = b; }
    public void setPaintMode(PaintMode m)          { paintMode = m; updateCursor(); }
    public void setColorPalette(ColorPalette p)    { palette = p; }
    public void setNeighborhoodType(NeighborhoodType nt) { neighborhoodType = nt; }
    public void setThresholds(double a, double b)  { thresholdA = a; thresholdB = b; }
    public void setCellListener(GridCellListener l){ cellListener = l; }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (grid == null) return;

        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int rows  = grid.getRows(), cols = grid.getCols();
        int cellW = Math.max(MIN_CELL_SIZE, getWidth()  / cols);
        int cellH = Math.max(MIN_CELL_SIZE, getHeight() / rows);
        boolean drawLines = cellW >= 4 && cellH >= 4;

        // ── Neighborhood highlight (drawn under cells) ──────────────────────
        if (hoverRow >= 0 && hoverCol >= 0) {
            g.setColor(new Color(255, 255, 100, 35));
            for (int[] off : neighborhoodType.offsets()) {
                int nr = hoverRow + off[0], nc = hoverCol + off[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols)
                    g.fillRect(nc * cellW, nr * cellH, cellW, cellH);
            }
        }

        // ── Cells ───────────────────────────────────────────────────────────
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * cellW, y = r * cellH;
                AgentType type = grid.getCell(r, c);

                Color fill;
                if (type == null) {
                    fill = EMPTY_COLOR;
                } else if (showHeatmap) {
                    double ratio = grid.getSatisfactionRatio(r, c, neighborhoodType);
                    fill = heatmapColor(ratio);
                } else {
                    fill = agentColor(type);
                }
                g.setColor(fill);
                g.fillRect(x, y, cellW, cellH);

                // Unsatisfied highlight border
                if (highlightUnsatisfied && type != null) {
                    double thr = (type == AgentType.TYPE_A) ? thresholdA : thresholdB;
                    if (!grid.isSatisfied(r, c, thr, neighborhoodType)) {
                        g.setColor(new Color(255, 160, 20, 200));
                        g.drawRect(x, y, cellW - 1, cellH - 1);
                    }
                }

                // Grid lines
                if (drawLines) {
                    g.setColor(GRID_LINE_COLOR);
                    g.drawRect(x, y, cellW, cellH);
                }
            }
        }

        // ── Hover focal cell outline ─────────────────────────────────────────
        if (hoverRow >= 0 && hoverCol >= 0
                && hoverRow < rows && hoverCol < cols) {
            g.setColor(new Color(255, 255, 100, 220));
            Stroke old = g.getStroke();
            g.setStroke(new BasicStroke(2f));
            g.drawRect(hoverCol * cellW + 1, hoverRow * cellH + 1, cellW - 2, cellH - 2);
            g.setStroke(old);

            paintHoverBox(g, hoverRow, hoverCol, cellW, cellH, rows, cols);
        }

        // ── Paint-mode cursor indicator ──────────────────────────────────────
        if (paintMode != PaintMode.OFF && hoverRow >= 0 && hoverCol >= 0) {
            Color cursorColor = switch (paintMode) {
                case PAINT_A -> agentColor(AgentType.TYPE_A);
                case PAINT_B -> agentColor(AgentType.TYPE_B);
                case ERASE   -> new Color(200, 200, 200);
                default      -> Color.WHITE;
            };
            g.setColor(new Color(cursorColor.getRed(), cursorColor.getGreen(), cursorColor.getBlue(), 100));
            g.fillRect(hoverCol * cellW, hoverRow * cellH, cellW, cellH);
        }
    }

    // ── Hover info box ────────────────────────────────────────────────────────

    private void paintHoverBox(Graphics2D g, int row, int col,
                                int cellW, int cellH, int rows, int cols) {
        AgentType type = grid.getCell(row, col);
        String typeStr = (type == null) ? "Empty" : type.getDisplayName();

        String satisfiedStr = "", ratioStr = "", nbrStr = "";
        if (type != null) {
            double ratio  = grid.getSatisfactionRatio(row, col, neighborhoodType);
            double thr    = (type == AgentType.TYPE_A) ? thresholdA : thresholdB;
            boolean sat   = ratio >= thr;
            var nbrs      = grid.getNeighbours(row, col, neighborhoodType);
            long countA   = nbrs.stream().filter(t -> t == AgentType.TYPE_A).count();
            long countB   = nbrs.stream().filter(t -> t == AgentType.TYPE_B).count();
            ratioStr      = String.format("Satisfaction: %.0f%%", ratio * 100);
            satisfiedStr  = sat ? "Status: satisfied" : "Status: unsatisfied";
            nbrStr        = String.format("Nbrs: %d  (%dA  %dB)", nbrs.size(), countA, countB);
        }

        String[] lines = {
            String.format("Cell (%d, %d)", row, col),
            typeStr,
            ratioStr,
            satisfiedStr,
            nbrStr
        };

        Font  font  = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        g.setFont(font);
        FontMetrics fm  = g.getFontMetrics();
        int lineH       = fm.getHeight();
        int pad         = 5;
        int boxW        = 0;
        for (String l : lines) if (!l.isEmpty()) boxW = Math.max(boxW, fm.stringWidth(l));
        boxW += pad * 2;
        int boxH = (int) (lines.length * lineH * 0.85) + pad * 2;

        // Position: prefer right of cell, flip if off-screen
        int bx = (col + 1) * cellW + 4;
        int by = row * cellH;
        if (bx + boxW > getWidth())  bx = col * cellW - boxW - 4;
        if (by + boxH > getHeight()) by = getHeight() - boxH - 4;
        bx = Math.max(0, bx);
        by = Math.max(0, by);

        // Background
        g.setColor(new Color(12, 14, 22, 220));
        g.fillRoundRect(bx, by, boxW, boxH, 6, 6);
        g.setColor(new Color(60, 80, 130));
        g.drawRoundRect(bx, by, boxW, boxH, 6, 6);

        // Text
        int ty = by + pad + fm.getAscent();
        for (String line : lines) {
            if (!line.isEmpty()) {
                g.setColor(new Color(200, 215, 240));
                g.drawString(line, bx + pad, ty);
            }
            ty += (int)(lineH * 0.85);
        }
    }

    // ── Color helpers ─────────────────────────────────────────────────────────

    private Color agentColor(AgentType type) {
        if (type == null) return EMPTY_COLOR;
        if (palette == ColorPalette.COLORBLIND)
            return (type == AgentType.TYPE_A) ? CB_TYPE_A : CB_TYPE_B;
        return type.getDisplayColor();
    }

    /** Green (1.0) → Amber (0.5) → Red (0.0) satisfaction heatmap. */
    private static Color heatmapColor(double ratio) {
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        if (ratio <= 0.5) {
            float t = (float)(ratio * 2.0);
            return new Color(255, (int)(170 * t), (int)(10 * t));
        } else {
            float t = (float)((ratio - 0.5) * 2.0);
            return new Color((int)(255 * (1 - t) + 50 * t),
                             (int)(170 * (1 - t) + 210 * t),
                             (int)(10  * (1 - t) + 60  * t));
        }
    }

    // ── Mouse / input ─────────────────────────────────────────────────────────

    private void updateHover(MouseEvent e) {
        if (grid == null) { hoverRow = hoverCol = -1; return; }
        int rows  = grid.getRows(), cols = grid.getCols();
        int cellW = Math.max(MIN_CELL_SIZE, getWidth()  / cols);
        int cellH = Math.max(MIN_CELL_SIZE, getHeight() / rows);
        hoverCol = e.getX() / cellW;
        hoverRow = e.getY() / cellH;
        if (hoverRow < 0 || hoverRow >= rows || hoverCol < 0 || hoverCol >= cols)
            hoverRow = hoverCol = -1;
    }

    private void handlePaint(MouseEvent e) {
        if (paintMode == PaintMode.OFF || grid == null || cellListener == null) return;
        updateHover(e);
        if (hoverRow < 0 || hoverCol < 0) return;
        AgentType placed = switch (paintMode) {
            case PAINT_A -> AgentType.TYPE_A;
            case PAINT_B -> AgentType.TYPE_B;
            case ERASE   -> null;
            default      -> null;
        };
        cellListener.onCellPainted(hoverRow, hoverCol, placed);
        repaint();
    }

    private void updateCursor() {
        setCursor(paintMode == PaintMode.OFF
            ? Cursor.getDefaultCursor()
            : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
}
