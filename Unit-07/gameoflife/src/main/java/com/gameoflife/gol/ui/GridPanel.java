package com.gameoflife.gol.ui;

import com.gameoflife.gol.GridState;
import com.gameoflife.gol.Pattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Canvas that renders the game grid and handles mouse-based editing.
 *
 * <h2>Edit modes</h2>
 * <ul>
 *   <li>Normal — left-click/drag paints alive; right-click/drag paints dead; toggle on click.</li>
 *   <li>Pattern placement — a ghost preview follows the cursor; click to place.</li>
 * </ul>
 *
 * <h2>Camera</h2>
 * <ul>
 *   <li>Mouse wheel: zoom (change cell size)</li>
 *   <li>Middle-mouse or Alt+drag: pan</li>
 * </ul>
 */
public final class GridPanel extends JPanel {

    // ── Colors (dark theme) ──────────────────────────────────────────────────
    private Color aliveColor = new Color(80, 220, 130);
    private Color deadColor  = new Color(10,  13,  22);
    private Color gridColor  = new Color(22,  28,  44);
    private Color ghostColor = new Color(80, 200, 130, 80);
    private Color hoverColor = new Color(60, 100, 180, 60);

    // ── Layout ───────────────────────────────────────────────────────────────
    private int cellSize    = 10;       // pixels per cell
    private int offsetX     = 0;        // pan offset in pixels
    private int offsetY     = 0;
    private static final int MIN_CELL = 2;
    private static final int MAX_CELL = 40;

    // ── State ────────────────────────────────────────────────────────────────
    private GridState state;
    private boolean   editable = true;

    // ── Callbacks ────────────────────────────────────────────────────────────
    private Consumer<int[]>   onToggle;           // (row, col)
    private Consumer<int[]>   onPaintAlive;
    private Consumer<int[]>   onPaintDead;
    private Consumer<int[]>   onPatternPlace;     // (row, col) — center of pattern

    // ── Pattern placement mode ───────────────────────────────────────────────
    private Pattern previewPattern = null;
    private int     previewRow = -1;
    private int     previewCol = -1;

    // ── Mouse drag state ─────────────────────────────────────────────────────
    private boolean  dragging      = false;
    private boolean  dragPainting  = false;   // true = paint alive, false = erase
    private int      dragPrevRow   = -1;
    private int      dragPrevCol   = -1;
    private boolean  panning       = false;
    private int      panStartX, panStartY;

    // ── Hover ────────────────────────────────────────────────────────────────
    private int hoverRow = -1, hoverCol = -1;

    public GridPanel() {
        setBackground(deadColor);
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handlePressed(e); }
            @Override public void mouseReleased(MouseEvent e) { dragging = panning = false; }
            @Override public void mouseDragged(MouseEvent e) { handleDragged(e); }
            @Override public void mouseMoved(MouseEvent e)   { handleMoved(e); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) { handleWheel(e); }
            @Override public void mouseExited(MouseEvent e)  { hoverRow = hoverCol = -1; repaint(); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    public void setState(GridState s) {
        this.state = s;
        repaint();
    }

    public void setEditable(boolean e) { this.editable = e; }

    // -------------------------------------------------------------------------
    // Pattern preview
    // -------------------------------------------------------------------------

    public void startPatternPlacement(Pattern p) {
        previewPattern = p;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        repaint();
    }

    public void cancelPatternPlacement() {
        previewPattern = null;
        previewRow = previewCol = -1;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    public boolean isInPatternMode() { return previewPattern != null; }

    // -------------------------------------------------------------------------
    // Callbacks
    // -------------------------------------------------------------------------

    public void setOnToggle(Consumer<int[]> cb)        { onToggle = cb; }
    public void setOnPaintAlive(Consumer<int[]> cb)    { onPaintAlive = cb; }
    public void setOnPaintDead(Consumer<int[]> cb)     { onPaintDead = cb; }
    public void setOnPatternPlace(Consumer<int[]> cb)  { onPatternPlace = cb; }

    // -------------------------------------------------------------------------
    // Colors / zoom
    // -------------------------------------------------------------------------

    public void setAliveColor(Color c) { aliveColor = c; repaint(); }
    public void setDeadColor(Color c)  { deadColor  = c; setBackground(c); repaint(); }
    public void setGridColor(Color c)  { gridColor  = c; repaint(); }

    public int  getCellSize()  { return cellSize; }
    public void setCellSize(int s) {
        cellSize = Math.max(MIN_CELL, Math.min(MAX_CELL, s));
        revalidate(); repaint();
    }

    public void centerView() {
        if (state == null) return;
        int panelW = getWidth(), panelH = getHeight();
        offsetX = (panelW - state.cols() * cellSize) / 2;
        offsetY = (panelH - state.rows() * cellSize) / 2;
        repaint();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == null) return;
        Graphics2D g2 = (Graphics2D) g.create();

        int rows = state.rows(), cols = state.cols();

        // ── Dead background ──────────────────────────────────────────────────
        g2.setColor(deadColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // ── Alive cells ──────────────────────────────────────────────────────
        g2.setColor(aliveColor);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (state.isAlive(r, c)) {
                    g2.fillRect(offsetX + c * cellSize, offsetY + r * cellSize,
                                cellSize, cellSize);
                }
            }
        }

        // ── Grid lines (only when cells are large enough) ────────────────────
        if (cellSize >= 4) {
            g2.setColor(gridColor);
            for (int c = 0; c <= cols; c++)
                g2.drawLine(offsetX + c * cellSize, offsetY,
                            offsetX + c * cellSize, offsetY + rows * cellSize);
            for (int r = 0; r <= rows; r++)
                g2.drawLine(offsetX, offsetY + r * cellSize,
                            offsetX + cols * cellSize, offsetY + r * cellSize);
        }

        // ── Hover highlight ──────────────────────────────────────────────────
        if (editable && hoverRow >= 0 && hoverCol >= 0 && previewPattern == null) {
            g2.setColor(hoverColor);
            g2.fillRect(offsetX + hoverCol * cellSize,
                        offsetY + hoverRow * cellSize,
                        cellSize, cellSize);
        }

        // ── Pattern ghost preview ─────────────────────────────────────────────
        if (previewPattern != null && previewRow >= 0) {
            int startR = previewRow - previewPattern.getHeight() / 2;
            int startC = previewCol - previewPattern.getWidth()  / 2;
            g2.setColor(ghostColor);
            for (int[] cell : previewPattern.getCells()) {
                int pr = startR + cell[0];
                int pc = startC + cell[1];
                if (pr >= 0 && pr < rows && pc >= 0 && pc < cols) {
                    g2.fillRect(offsetX + pc * cellSize,
                                offsetY + pr * cellSize,
                                cellSize, cellSize);
                }
            }
            // Draw bounding box
            g2.setColor(new Color(100, 200, 150, 160));
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{4, 3}, 0));
            g2.drawRect(offsetX + startC * cellSize, offsetY + startR * cellSize,
                        previewPattern.getWidth()  * cellSize,
                        previewPattern.getHeight() * cellSize);
        }

        // ── Border ───────────────────────────────────────────────────────────
        g2.setColor(new Color(40, 55, 80));
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(offsetX, offsetY, cols * cellSize, rows * cellSize);

        g2.dispose();
    }

    // -------------------------------------------------------------------------
    // Mouse handlers
    // -------------------------------------------------------------------------

    private void handlePressed(MouseEvent e) {
        requestFocusInWindow();
        int[] rc = screenToCell(e.getX(), e.getY());
        if (rc == null) return;

        // Pattern placement mode
        if (previewPattern != null) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (onPatternPlace != null) onPatternPlace.accept(rc);
                cancelPatternPlacement();
            } else if (SwingUtilities.isRightMouseButton(e)) {
                cancelPatternPlacement();
            }
            return;
        }

        if (!editable) return;

        // Pan mode: middle button or alt+left
        if (SwingUtilities.isMiddleMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) && e.isAltDown())) {
            panning   = true;
            panStartX = e.getX();
            panStartY = e.getY();
            return;
        }

        dragging     = true;
        dragPainting = SwingUtilities.isLeftMouseButton(e);
        dragPrevRow  = rc[0];
        dragPrevCol  = rc[1];

        if (dragPainting) {
            if (onToggle != null) onToggle.accept(rc);
        } else {
            if (onPaintDead != null) onPaintDead.accept(rc);
        }
    }

    private void handleDragged(MouseEvent e) {
        int[] rc = screenToCell(e.getX(), e.getY());

        // Update pattern preview
        if (previewPattern != null) {
            if (rc != null) { previewRow = rc[0]; previewCol = rc[1]; }
            repaint();
            return;
        }

        if (panning) {
            offsetX += e.getX() - panStartX;
            offsetY += e.getY() - panStartY;
            panStartX = e.getX();
            panStartY = e.getY();
            repaint();
            return;
        }

        if (!dragging || !editable || rc == null) return;
        if (rc[0] == dragPrevRow && rc[1] == dragPrevCol) return;

        dragPrevRow = rc[0];
        dragPrevCol = rc[1];

        if (dragPainting) {
            if (onPaintAlive != null) onPaintAlive.accept(rc);
        } else {
            if (onPaintDead != null) onPaintDead.accept(rc);
        }
        repaint();
    }

    private void handleMoved(MouseEvent e) {
        int[] rc = screenToCell(e.getX(), e.getY());
        if (previewPattern != null && rc != null) {
            previewRow = rc[0];
            previewCol = rc[1];
            repaint();
            return;
        }
        hoverRow = (rc != null) ? rc[0] : -1;
        hoverCol = (rc != null) ? rc[1] : -1;
        repaint();
    }

    private void handleWheel(MouseWheelEvent e) {
        if (state == null) return;
        int oldCell = cellSize;
        int delta   = e.getWheelRotation() < 0 ? 1 : -1;
        int newCell = Math.max(MIN_CELL, Math.min(MAX_CELL, cellSize + delta));
        if (newCell == oldCell) return;

        // Zoom toward mouse position
        int mx = e.getX(), my = e.getY();
        double worldCol = (double)(mx - offsetX) / oldCell;
        double worldRow = (double)(my - offsetY) / oldCell;
        cellSize = newCell;
        offsetX  = (int) Math.round(mx - worldCol * cellSize);
        offsetY  = (int) Math.round(my - worldRow * cellSize);
        repaint();
    }

    // -------------------------------------------------------------------------
    // Coordinate conversion
    // -------------------------------------------------------------------------

    private int[] screenToCell(int sx, int sy) {
        if (state == null || cellSize <= 0) return null;
        int c = (sx - offsetX) / cellSize;
        int r = (sy - offsetY) / cellSize;
        // Allow slight margin outside
        if (r < 0 || r >= state.rows() || c < 0 || c >= state.cols()) return null;
        return new int[]{r, c};
    }

    /** Converts a cell position to screen coordinates (top-left of cell). */
    public Point cellToScreen(int row, int col) {
        return new Point(offsetX + col * cellSize, offsetY + row * cellSize);
    }
}
