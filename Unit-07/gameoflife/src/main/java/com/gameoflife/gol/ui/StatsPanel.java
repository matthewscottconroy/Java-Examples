package com.gameoflife.gol.ui;

import com.gameoflife.gol.GameController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Side panel showing real-time statistics and controls summary.
 */
public final class StatsPanel extends JPanel {

    private static final Color BG     = new Color(12, 14, 24);
    private static final Color FG     = new Color(190, 210, 235);
    private static final Color ACCENT = new Color(80, 200, 140);
    private static final Color DIM    = new Color(100, 115, 140);
    private static final Color BORN   = new Color(80, 200, 120);
    private static final Color DIED   = new Color(220, 80,  80);
    private static final Font  MONO   = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    private static final Font  HEADER = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    private final JLabel genLabel   = makeLabel("0");
    private final JLabel popLabel   = makeLabel("0");
    private final JLabel bornLabel  = makeLabel("0");
    private final JLabel diedLabel  = makeLabel("0");
    private final JLabel ruleLabel  = makeLabel("B3/S23");
    private final JLabel sizeLabel  = makeLabel("80 × 60");
    private final JLabel histLabel  = makeLabel("0 / 1000");
    private final JLabel fpsLabel   = makeLabel("--");

    public StatsPanel() {
        setBackground(BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setPreferredSize(new Dimension(170, 400));

        add(section("SIMULATION"));
        add(row("Generation", genLabel));
        add(row("Population", popLabel));
        add(Box.createVerticalStrut(4));
        add(row("Born (last)", makeDynLabel(bornLabel, BORN)));
        add(row("Died (last)", makeDynLabel(diedLabel, DIED)));
        add(Box.createVerticalStrut(8));

        add(section("SETTINGS"));
        add(row("Rule", ruleLabel));
        add(row("Grid size", sizeLabel));
        add(Box.createVerticalStrut(8));

        add(section("HISTORY"));
        add(row("Frames", histLabel));
        add(Box.createVerticalStrut(8));

        add(section("PLAYBACK"));
        add(row("Rate", fpsLabel));
        add(Box.createVerticalGlue());

        add(Box.createVerticalStrut(12));
        JLabel hint = new JLabel("<html><font color='#606878'>Mouse wheel: zoom<br>" +
            "Middle/Alt+drag: pan<br>Left click: toggle<br>" +
            "Right drag: erase<br>Space: play/pause<br>" +
            "← →: step back/fwd</font></html>");
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        hint.setBorder(new EmptyBorder(4, 0, 0, 0));
        add(hint);
    }

    /** Updates all statistics from the given controller. */
    public void update(GameController ctrl, int fps) {
        genLabel.setText(String.format("%,d", ctrl.getGeneration()));
        popLabel.setText(String.format("%,d", ctrl.getCurrent().population()));
        bornLabel.setText(String.format("+%,d", ctrl.getLastBorn()));
        diedLabel.setText(String.format("−%,d", ctrl.getLastDied()));
        ruleLabel.setText(ctrl.getRuleSet().toNotation());
        sizeLabel.setText(ctrl.getCurrent().rows() + " × " + ctrl.getCurrent().cols());
        histLabel.setText(ctrl.historySize() + " / " + GameController.MAX_HISTORY);
        fpsLabel.setText(fps + " gen/s");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MONO);
        l.setForeground(FG);
        return l;
    }

    private static JLabel makeDynLabel(JLabel l, Color c) {
        l.setForeground(c);
        return l;
    }

    private JPanel row(String key, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        JLabel k = new JLabel(key);
        k.setFont(MONO);
        k.setForeground(DIM);
        p.add(k, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        return p;
    }

    private JLabel section(String title) {
        JLabel l = new JLabel(title);
        l.setFont(HEADER);
        l.setForeground(ACCENT);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 50, 70)),
            new EmptyBorder(4, 0, 2, 0)));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
}
