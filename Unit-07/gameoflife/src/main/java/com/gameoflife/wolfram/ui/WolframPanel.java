package com.gameoflife.wolfram.ui;

import com.gameoflife.wolfram.CAHistory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Canvas that draws the Wolfram 1D CA space-time diagram.
 *
 * <p>Each row of the diagram represents one generation. Row 0 (the initial
 * condition) is at the top; newer generations grow downward. When the diagram
 * exceeds the visible area, a JScrollPane should wrap this component and
 * {@link #scrollToBottom(JScrollPane)} can be called after each step.
 *
 * <h2>Zoom</h2>
 * Mouse wheel changes the cell size; the diagram updates immediately.
 */
public final class WolframPanel extends JPanel {

    private static final Color DEFAULT_ALIVE = new Color(220, 235, 255);
    private static final Color DEFAULT_DEAD  = new Color(10, 12, 22);
    private static final Color GEN0_BORDER   = new Color(80, 200, 140, 160);

    private Color aliveColor = DEFAULT_ALIVE;
    private Color deadColor  = DEFAULT_DEAD;
    private int   cellSize   = 4;

    private CAHistory history;

    // For highlighting the current generation row
    private Color currentRowColor = new Color(80, 160, 255, 60);

    public WolframPanel() {
        setBackground(DEFAULT_DEAD);

        addMouseWheelListener(e -> {
            int delta = e.getWheelRotation() < 0 ? 1 : -1;
            cellSize = Math.max(1, Math.min(20, cellSize + delta));
            revalidate();
            repaint();
        });
    }

    // -------------------------------------------------------------------------
    // Data
    // -------------------------------------------------------------------------

    public void setHistory(CAHistory h) {
        this.history = h;
        revalidate();
        repaint();
    }

    public void setAliveColor(Color c) { aliveColor = c; repaint(); }
    public void setDeadColor(Color c)  { deadColor  = c; setBackground(c); repaint(); }
    public int  getCellSize()          { return cellSize; }
    public void setCellSize(int s)     { cellSize = Math.max(1, Math.min(20, s)); revalidate(); repaint(); }

    // -------------------------------------------------------------------------
    // Preferred size (drives JScrollPane)
    // -------------------------------------------------------------------------

    @Override
    public Dimension getPreferredSize() {
        if (history == null) return new Dimension(800, 600);
        return new Dimension(history.width() * cellSize,
                             history.generations() * cellSize);
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (history == null) {
            g.setColor(new Color(80, 100, 140));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            g.drawString("Select a rule and initial condition to begin.", 30, 40);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        // Determine visible row range (for performance with large histories)
        Rectangle clip = g.getClipBounds();
        if (clip == null) clip = new Rectangle(0, 0, getWidth(), getHeight());

        int firstRow = Math.max(0, clip.y / cellSize);
        int lastRow  = Math.min(history.generations() - 1,
                                (clip.y + clip.height) / cellSize + 1);

        List<boolean[]> rows = history.allRows();
        for (int gen = firstRow; gen <= lastRow; gen++) {
            boolean[] row = rows.get(gen);
            int y = gen * cellSize;
            for (int col = 0; col < row.length; col++) {
                g2.setColor(row[col] ? aliveColor : deadColor);
                g2.fillRect(col * cellSize, y, cellSize, cellSize);
            }
        }

        // Highlight generation 0 boundary
        g2.setColor(GEN0_BORDER);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(0, cellSize, getWidth(), cellSize);

        // Highlight last row (current generation)
        int lastGen = history.generations() - 1;
        g2.setColor(currentRowColor);
        g2.fillRect(0, lastGen * cellSize, getWidth(), cellSize);

        // Grid lines for large cells
        if (cellSize >= 6) {
            g2.setColor(new Color(30, 36, 58, 120));
            for (int col = 0; col <= history.width(); col++)
                g2.drawLine(col * cellSize, firstRow * cellSize,
                            col * cellSize, (lastRow + 1) * cellSize);
        }

        g2.dispose();
    }

    // -------------------------------------------------------------------------
    // Scroll helpers
    // -------------------------------------------------------------------------

    /** Scrolls the parent JScrollPane to show the last (most recent) generation. */
    public void scrollToBottom(JScrollPane scrollPane) {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vb = scrollPane.getVerticalScrollBar();
            vb.setValue(vb.getMaximum());
        });
    }

    /** Scrolls the parent JScrollPane to show generation 0. */
    public void scrollToTop(JScrollPane scrollPane) {
        SwingUtilities.invokeLater(() ->
            scrollPane.getVerticalScrollBar().setValue(0));
    }
}
