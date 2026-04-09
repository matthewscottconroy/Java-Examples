package com.bouncingballs.ui;

import com.bouncingballs.model.Ball;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Bottom control strip for the glass-jar simulator.
 *
 * <p>Provides:
 * <ul>
 *   <li>Gravity slider (0 – 2 000 px/s²)</li>
 *   <li>Size toggle: Small / Medium / Large</li>
 *   <li>Density toggle: Light / Medium / Heavy</li>
 *   <li>Add Ball and Clear buttons</li>
 *   <li>Live ball-count label</li>
 * </ul>
 */
public class ControlPanel extends JPanel {

    private static final Color BG         = new Color(14, 17, 32);
    private static final Color FG         = new Color(180, 210, 255);
    private static final Color ACCENT     = new Color(70, 130, 200);
    private static final Color BTN_SEL    = new Color(55, 110, 200);
    private static final Color BTN_UNSEL  = new Color(30, 38, 68);
    private static final Color BTN_BORDER = new Color(70, 100, 170);

    private final BallPanel sim;

    private final JSlider  gravitySlider;
    private final JLabel   gravityLabel;
    private final JLabel   ballCountLabel;

    private Ball.Size    selectedSize    = Ball.Size.MEDIUM;
    private Ball.Density selectedDensity = Ball.Density.MEDIUM;

    private final JButton[] sizeButtons    = new JButton[3];
    private final JButton[] densityButtons = new JButton[3];

    public ControlPanel(BallPanel sim) {
        this.sim = sim;
        setBackground(BG);
        setBorder(new EmptyBorder(6, 10, 6, 10));
        setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 6, 3, 6);
        gc.fill   = GridBagConstraints.BOTH;

        // ── Row 0: gravity label + slider ──────────────────────────────────────
        gc.gridy = 0; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1;
        add(label("Gravity"), gc);

        gravitySlider = new JSlider(0, 2000, 800);
        gravitySlider.setBackground(BG);
        gravitySlider.setForeground(FG);
        gravitySlider.setMajorTickSpacing(500);
        gravitySlider.setPaintTicks(true);
        gravitySlider.setPreferredSize(new Dimension(180, 36));
        gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 2;
        add(gravitySlider, gc);

        gravityLabel = label("800 px/s²");
        gc.gridx = 3; gc.weightx = 0; gc.gridwidth = 1;
        add(gravityLabel, gc);

        gravitySlider.addChangeListener(e -> {
            int v = gravitySlider.getValue();
            gravityLabel.setText(v + " px/s²");
            sim.setGravity(v);
        });

        // Gravity presets
        gc.gridx = 4; gc.gridwidth = 1;
        add(smallButton("Moon",    () -> setGravity(162)),  gc);
        gc.gridx = 5;
        add(smallButton("Earth",   () -> setGravity(800)),  gc);
        gc.gridx = 6;
        add(smallButton("Jupiter", () -> setGravity(2000)), gc);

        // ── Row 1: size + density toggles + actions ────────────────────────────
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1;
        add(label("Size"), gc);

        Ball.Size[]    sizes     = Ball.Size.values();
        String[]       sizeNames = {"S", "M", "L"};
        for (int i = 0; i < 3; i++) {
            final Ball.Size s = sizes[i];
            sizeButtons[i] = toggleButton(sizeNames[i], i == 1, () -> {
                selectedSize = s;
                sim.setPendingSize(s);
                refreshSizeButtons(s);
            });
            gc.gridx = 1 + i; gc.weightx = 0;
            add(sizeButtons[i], gc);
        }

        gc.gridx = 4; gc.weightx = 0;
        add(label("Density"), gc);

        Ball.Density[] densities     = Ball.Density.values();
        String[]       densityNames  = {"Light", "Med", "Heavy"};
        for (int i = 0; i < 3; i++) {
            final Ball.Density d = densities[i];
            densityButtons[i] = toggleButton(densityNames[i], i == 1, () -> {
                selectedDensity = d;
                sim.setPendingDensity(d);
                refreshDensityButtons(d);
            });
            gc.gridx = 5 + i; gc.weightx = 0;
            add(densityButtons[i], gc);
        }

        // ── Row 1 right: Add / Clear ───────────────────────────────────────────
        JButton addBtn = actionButton("Add Ball", new Color(50, 130, 80), () -> {
            // Drop a ball from the top-centre of the jar
            sim.addBall(BallPanel.PANEL_W / 2.0 + (Math.random() - 0.5) * 80, 60);
        });
        gc.gridy = 0; gc.gridx = 7; gc.gridwidth = 1; gc.weightx = 0;
        add(addBtn, gc);

        JButton clearBtn = actionButton("Clear", new Color(140, 50, 50), sim::clearBalls);
        gc.gridy = 1; gc.gridx = 7;
        add(clearBtn, gc);

        // Ball count
        ballCountLabel = label("Balls: 0");
        ballCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 0; gc.gridx = 8; gc.gridheight = 2; gc.fill = GridBagConstraints.VERTICAL;
        add(ballCountLabel, gc);

        // Kick off count updater
        new Timer(200, e -> ballCountLabel.setText("Balls: " + sim.getBallCount())).start();
    }

    // ── Gravity helpers ───────────────────────────────────────────────────────

    private void setGravity(int g) {
        gravitySlider.setValue(g);
        gravityLabel.setText(g + " px/s²");
        sim.setGravity(g);
    }

    // ── Button refresh ────────────────────────────────────────────────────────

    private void refreshSizeButtons(Ball.Size selected) {
        Ball.Size[] vals = Ball.Size.values();
        for (int i = 0; i < sizeButtons.length; i++) {
            setSelected(sizeButtons[i], vals[i] == selected);
        }
    }

    private void refreshDensityButtons(Ball.Density selected) {
        Ball.Density[] vals = Ball.Density.values();
        for (int i = 0; i < densityButtons.length; i++) {
            setSelected(densityButtons[i], vals[i] == selected);
        }
    }

    private static void setSelected(JButton btn, boolean sel) {
        btn.setBackground(sel ? BTN_SEL : BTN_UNSEL);
        btn.setForeground(sel ? Color.WHITE : FG);
    }

    // ── Widget factories ──────────────────────────────────────────────────────

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private static JButton toggleButton(String text, boolean initSelected, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(initSelected ? BTN_SEL : BTN_UNSEL);
        btn.setForeground(initSelected ? Color.WHITE : FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BTN_BORDER, 1),
                new EmptyBorder(3, 8, 3, 8)));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(56, 28));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private static JButton smallButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setBackground(BTN_UNSEL);
        btn.setForeground(FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BTN_BORDER, 1),
                new EmptyBorder(2, 6, 2, 6)));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(60, 26));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private static JButton actionButton(String text, Color bg, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.brighter(), 1),
                new EmptyBorder(4, 10, 4, 10)));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(80, 28));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}
