package com.gameoflife.gol.ui;

import com.gameoflife.gol.RuleSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Settings dialog for grid size, rules, wrapping, and display options.
 */
public final class SettingsDialog extends JDialog {

    private static final Color BG     = new Color(14, 16, 28);
    private static final Color BG2    = new Color(20, 24, 38);
    private static final Color FG     = new Color(190, 210, 235);
    private static final Color ACCENT = new Color(80, 200, 140);
    private static final Color DIM    = new Color(110, 130, 155);
    private static final Font  LABEL  = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final Font  MONO   = new Font(Font.MONOSPACED, Font.PLAIN, 11);

    // Result record
    public record Settings(
        int rows, int cols, boolean toroidal,
        RuleSet ruleSet,
        Color aliveColor, Color deadColor, Color gridColor,
        int cellSize
    ) {}

    private Settings result = null;

    public SettingsDialog(Frame owner, Settings current) {
        super(owner, "Settings", true);
        setBackground(BG);

        // ── Board size ────────────────────────────────────────────────────────
        JSpinner rowsSpin = spin(current.rows(), 10, 2000, 10);
        JSpinner colsSpin = spin(current.cols(), 10, 2000, 10);
        JCheckBox toroidal = check("Toroidal (edges wrap around)", current.toroidal());

        // ── Rule set ──────────────────────────────────────────────────────────
        JComboBox<String> presetBox = new JComboBox<>();
        for (RuleSet r : RuleSet.PRESETS) presetBox.addItem(r.toString());
        presetBox.addItem("Custom…");
        styleCombo(presetBox);

        JTextField customField = new JTextField(current.ruleSet().toNotation(), 12);
        customField.setFont(MONO);
        customField.setBackground(BG2);
        customField.setForeground(FG);
        customField.setCaretColor(FG);

        // Select current rule in combo
        RuleSet cur = current.ruleSet();
        boolean found = false;
        for (int i = 0; i < RuleSet.PRESETS.size(); i++) {
            if (RuleSet.PRESETS.get(i).equals(cur)) { presetBox.setSelectedIndex(i); found = true; break; }
        }
        if (!found) presetBox.setSelectedItem("Custom…");

        presetBox.addActionListener(e -> {
            int idx = presetBox.getSelectedIndex();
            if (idx < RuleSet.PRESETS.size()) {
                customField.setText(RuleSet.PRESETS.get(idx).toNotation());
            }
        });

        // ── Display ───────────────────────────────────────────────────────────
        JButton aliveBtn = colorBtn("Alive cells", current.aliveColor());
        JButton deadBtn  = colorBtn("Dead cells",  current.deadColor());
        JButton gridBtn  = colorBtn("Grid lines",  current.gridColor());

        wireColorPicker(aliveBtn, owner);
        wireColorPicker(deadBtn,  owner);
        wireColorPicker(gridBtn,  owner);

        JSpinner cellSpin = spin(current.cellSize(), 2, 40, 1);

        // ── Layout ───────────────────────────────────────────────────────────
        JPanel boardPanel = section("Board");
        boardPanel.add(row("Rows",    rowsSpin));
        boardPanel.add(row("Columns", colsSpin));
        boardPanel.add(toroidal);

        JPanel rulePanel = section("Rule Set");
        rulePanel.add(row("Preset",  presetBox));
        rulePanel.add(row("Notation", customField));

        JPanel dispPanel = section("Display");
        dispPanel.add(row("Cell size (px)",  cellSpin));
        dispPanel.add(row("",  aliveBtn));
        dispPanel.add(row("",  deadBtn));
        dispPanel.add(row("",  gridBtn));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(8, 10, 4, 10));
        main.add(boardPanel);
        main.add(Box.createVerticalStrut(6));
        main.add(rulePanel);
        main.add(Box.createVerticalStrut(6));
        main.add(dispPanel);

        // ── Buttons ───────────────────────────────────────────────────────────
        JButton applyBtn  = btn("Apply");
        JButton cancelBtn = btn("Cancel");

        applyBtn.addActionListener(e -> {
            RuleSet ruleSet;
            try { ruleSet = RuleSet.fromNotation(customField.getText().trim()); }
            catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid rule: " + ex.getMessage(),
                    "Rule Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            result = new Settings(
                num(rowsSpin), num(colsSpin), toroidal.isSelected(),
                ruleSet,
                getColor(aliveBtn), getColor(deadBtn), getColor(gridBtn),
                num(cellSpin)
            );
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnRow.setBackground(BG);
        btnRow.add(cancelBtn);
        btnRow.add(applyBtn);

        setLayout(new BorderLayout());
        add(main,   BorderLayout.CENTER);
        add(btnRow, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Returns the settings chosen, or null if cancelled. */
    public Settings getResult() { return result; }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private JPanel section(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(40, 55, 80), 1), title);
        tb.setTitleColor(ACCENT);
        tb.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        p.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(4, 6, 6, 6)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return p;
    }

    private JPanel row(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setBackground(BG);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        if (!label.isEmpty()) {
            JLabel l = new JLabel(label);
            l.setFont(LABEL);
            l.setForeground(DIM);
            l.setPreferredSize(new Dimension(110, 20));
            p.add(l, BorderLayout.WEST);
        }
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private static JSpinner spin(int val, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        s.setPreferredSize(new Dimension(80, 24));
        s.setBackground(BG2);
        if (s.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(BG2);
            de.getTextField().setForeground(FG);
            de.getTextField().setFont(MONO);
        }
        return s;
    }

    private static int num(JSpinner s) { return ((Number) s.getValue()).intValue(); }

    private static JCheckBox check(String text, boolean sel) {
        JCheckBox cb = new JCheckBox(text, sel);
        cb.setBackground(BG);
        cb.setForeground(FG);
        cb.setFont(LABEL);
        cb.setFocusPainted(false);
        return cb;
    }

    private static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(BG2);
        cb.setForeground(FG);
        cb.setFont(MONO);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
    }

    private static JButton colorBtn(String text, Color initial) {
        JButton b = new JButton(text);
        b.setBackground(initial);
        b.setForeground(luminance(initial) > 0.4 ? Color.BLACK : Color.WHITE);
        b.setFont(LABEL);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(180, 26));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return b;
    }

    private static void wireColorPicker(JButton btn, Component parent) {
        btn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(parent, "Choose " + btn.getText(), btn.getBackground());
            if (c != null) {
                btn.setBackground(c);
                btn.setForeground(luminance(c) > 0.4 ? Color.BLACK : Color.WHITE);
            }
        });
    }

    private static Color getColor(JButton btn) { return btn.getBackground(); }

    private static double luminance(Color c) {
        return (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
    }

    private static JButton btn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(28, 35, 55));
        b.setForeground(FG);
        b.setFont(LABEL);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 65, 90)),
            new EmptyBorder(4, 12, 4, 12)));
        return b;
    }
}
