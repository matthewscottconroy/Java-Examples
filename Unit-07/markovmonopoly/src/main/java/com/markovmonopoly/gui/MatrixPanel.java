package com.markovmonopoly.gui;

import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.core.TransitionMatrix;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Editable transition-matrix panel.
 *
 * <h2>What it shows</h2>
 * <p>A scrollable {@link JTable} where rows represent "from" states and columns
 * represent "to" states. Each cell is editable: type a new probability value and
 * press Enter. After editing, click <b>Normalize</b> to rescale each row to sum
 * to 1, then <b>Apply</b> to propagate the updated chain to all other panels.
 *
 * <h2>Color coding</h2>
 * <ul>
 *   <li>Diagonal (self-loop) cells have a slightly lighter background.</li>
 *   <li>Cells with probability 0 are dim; non-zero cells are brighter.</li>
 *   <li>Cells in a row whose sum deviates from 1.0 are flagged in amber.</li>
 * </ul>
 */
public final class MatrixPanel extends JPanel {

    private static final Color BG         = new Color(22, 28, 44);
    private static final Color BG_CELL    = new Color(32, 38, 58);
    private static final Color BG_DIAG    = new Color(40, 48, 74);
    private static final Color BG_NONZERO = new Color(36, 58, 80);
    private static final Color BG_INVALID = new Color(80, 55, 20);
    private static final Color FG_ZERO    = new Color(70, 90, 130);
    private static final Color FG_NONZERO = new Color(200, 230, 255);
    private static final Color FG_HEADER  = new Color(180, 200, 240);

    private final MarkovAppFrame app;

    private MatrixTableModel tableModel;
    private JTable           table;
    private JLabel           statusLabel;

    // ── Construction ──────────────────────────────────────────────────────────

    public MatrixPanel(MarkovAppFrame app) {
        this.app = app;
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        add(buildTable(),    BorderLayout.CENTER);
        add(buildToolbar(),  BorderLayout.SOUTH);
        add(buildLegend(),   BorderLayout.NORTH);
    }

    public void onChainChanged(MarkovChain chain) {
        tableModel = new MatrixTableModel(chain);
        table.setModel(tableModel);
        styleTable(chain);
        statusLabel.setText("Loaded: " + chain.getName() + " (" + chain.size() + " states)");
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    private JScrollPane buildTable() {
        tableModel = new MatrixTableModel(null);
        table = new JTable(tableModel);
        table.setBackground(BG_CELL);
        table.setForeground(FG_NONZERO);
        table.setGridColor(new Color(45, 55, 85));
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        table.setRowHeight(22);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionBackground(new Color(60, 90, 160));
        table.setSelectionForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new CellRenderer());

        // Row header (from-state labels) via a fixed column
        table.getTableHeader().setBackground(new Color(30, 38, 60));
        table.getTableHeader().setForeground(FG_HEADER);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG_CELL);
        sp.setBorder(BorderFactory.createLineBorder(new Color(50, 65, 110)));
        return sp;
    }

    private void styleTable(MarkovChain chain) {
        int n = chain.size();
        int colW = Math.min(72, Math.max(52, 500 / Math.max(n, 1)));
        for (int c = 0; c <= n; c++) {
            int w = (c == 0) ? 90 : colW;
            table.getColumnModel().getColumn(c).setPreferredWidth(w);
        }
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(new Color(26, 32, 52));

        JButton normalizeBtn = styledBtn("Normalize rows");
        normalizeBtn.setToolTipText("Rescale each row so that probabilities sum to 1");
        normalizeBtn.addActionListener(this::normalizeAction);

        JButton applyBtn = styledBtn("Apply & update");
        applyBtn.setBackground(new Color(40, 110, 60));
        applyBtn.setToolTipText("Push the current matrix to all other panels");
        applyBtn.addActionListener(this::applyAction);

        JButton resetBtn = styledBtn("Reset");
        resetBtn.setBackground(new Color(80, 55, 35));
        resetBtn.setToolTipText("Reload the original chain from the selector");
        resetBtn.addActionListener(e -> app.pushChain(app.getCurrentChain()));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(130, 160, 210));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

        p.add(normalizeBtn); p.add(applyBtn); p.add(resetBtn);
        p.add(Box.createHorizontalStrut(16));
        p.add(statusLabel);
        return p;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        p.setBackground(new Color(26, 32, 52));
        addLegendEntry(p, BG_NONZERO, "Non-zero probability");
        addLegendEntry(p, BG_DIAG,    "Self-loop (diagonal)");
        addLegendEntry(p, BG_INVALID, "Row sum ≠ 1 (needs normalization)");
        JLabel hint = new JLabel("  Click a cell to edit, then press Enter");
        hint.setForeground(new Color(110, 130, 170));
        hint.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        p.add(hint);
        return p;
    }

    private void addLegendEntry(JPanel p, Color bg, String text) {
        JLabel swatch = new JLabel("  ");
        swatch.setOpaque(true);
        swatch.setBackground(bg);
        swatch.setPreferredSize(new Dimension(16, 12));
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(140, 160, 200));
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        p.add(swatch); p.add(lbl);
    }

    private static JButton styledBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(50, 75, 130));
        b.setForeground(Color.WHITE);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setFocusPainted(false);
        return b;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void normalizeAction(ActionEvent e) {
        if (tableModel == null || tableModel.chain == null) return;
        // Stop any active cell editing
        if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing();
        tableModel.normalizeRows();
        statusLabel.setText("Rows normalized. Click 'Apply & update' to propagate.");
        table.repaint();
    }

    private void applyAction(ActionEvent e) {
        if (tableModel == null || tableModel.chain == null) return;
        if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing();

        MarkovChain old = tableModel.chain;
        double[][] data = tableModel.getData();
        MarkovChain updated = new MarkovChain(
            old.getName(), old.getDescription(),
            old.getStateLabels(), TransitionMatrix.of(data)
        );
        try {
            updated.getMatrix().validate();
            app.pushChain(updated);
            statusLabel.setText("Applied. All panels updated.");
        } catch (IllegalStateException ex) {
            statusLabel.setText("⚠ " + ex.getMessage() + " — normalize first.");
        }
    }

    // ── Table model ───────────────────────────────────────────────────────────

    private static final class MatrixTableModel extends AbstractTableModel {
        MarkovChain chain;
        double[][]  data;
        int         n;

        MatrixTableModel(MarkovChain chain) {
            this.chain = chain;
            if (chain != null) {
                n    = chain.size();
                data = chain.getMatrix().toArray();
            } else {
                n    = 0;
                data = new double[0][0];
            }
        }

        @Override public int getRowCount()    { return n; }
        @Override public int getColumnCount() { return n + 1; } // col 0 = row label

        @Override
        public String getColumnName(int col) {
            if (col == 0) return "From \\ To";
            return chain.getLabel(col - 1);
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) return chain.getLabel(row);
            return String.format("%.4f", data[row][col - 1]);
        }

        @Override public boolean isCellEditable(int row, int col) { return col > 0; }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0) return;
            try {
                double v = Double.parseDouble(value.toString().trim());
                if (v < 0) v = 0;
                data[row][col - 1] = v;
                fireTableCellUpdated(row, col);
            } catch (NumberFormatException ignored) {}
        }

        void normalizeRows() {
            for (int i = 0; i < n; i++) {
                double sum = 0;
                for (double v : data[i]) sum += v;
                if (sum > 0) {
                    for (int j = 0; j < n; j++) data[i][j] /= sum;
                } else {
                    for (int j = 0; j < n; j++) data[i][j] = 1.0 / n;
                }
            }
            fireTableDataChanged();
        }

        double[][] getData() { return data; }

        double rowSum(int row) {
            double s = 0;
            for (double v : data[row]) s += v;
            return s;
        }
    }

    // ── Renderer ─────────────────────────────────────────────────────────────

    private class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.RIGHT);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

            if (isSelected) {
                lbl.setBackground(new Color(60, 90, 160));
                lbl.setForeground(Color.WHITE);
                return lbl;
            }

            if (col == 0) {
                lbl.setBackground(new Color(28, 36, 58));
                lbl.setForeground(FG_HEADER);
                lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                return lbl;
            }

            if (tableModel == null) return lbl;

            double prob = tableModel.data[row][col - 1];
            double rowSum = tableModel.rowSum(row);
            boolean badRow = Math.abs(rowSum - 1.0) > 0.005;
            boolean diag   = (row == col - 1);

            if (badRow) {
                lbl.setBackground(BG_INVALID);
                lbl.setForeground(new Color(255, 200, 100));
            } else if (diag) {
                lbl.setBackground(BG_DIAG);
                lbl.setForeground(prob > 0 ? FG_NONZERO : FG_ZERO);
            } else if (prob > 0) {
                lbl.setBackground(BG_NONZERO);
                lbl.setForeground(FG_NONZERO);
            } else {
                lbl.setBackground(BG_CELL);
                lbl.setForeground(FG_ZERO);
            }

            return lbl;
        }
    }
}
