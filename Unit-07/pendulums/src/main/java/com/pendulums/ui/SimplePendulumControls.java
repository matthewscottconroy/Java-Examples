package com.pendulums.ui;

import com.pendulums.model.SimplePendulum;
import com.pendulums.physics.Integrator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the simple pendulum simulation.
 *
 * <p>Provides sliders for rod length, gravity, and damping, a toggle for the
 * integration method, a trail visibility checkbox, a ghost-pendulum checkbox,
 * and a reset button.
 */
class SimplePendulumControls extends JPanel {

    private static final Color BG    = new Color(15, 17, 30);
    private static final Color FG    = new Color(200, 210, 230);
    private static final Color TRACK = new Color(60, 70, 100);

    private final SimplePendulum     model;
    private final SimplePendulumPanel panel;

    SimplePendulumControls(SimplePendulum model, SimplePendulumPanel panel) {
        this.model = model;
        this.panel = panel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        add(namedSlider("Length (px)", 60, 350, (int) model.getLength(),
                v -> { model.setLength(v); panel.syncGhost(); }));

        add(namedSlider("Gravity (px/s²)", 100, 2000, (int) model.getGravity(),
                v -> { model.setGravity(v); panel.syncGhost(); }));

        add(namedSlider("Damping (×0.001)", 0, 100, (int)(model.getDamping() * 1000),
                v -> model.setDamping(v / 1000.0)));

        add(namedSlider("Angle (°)", -170, 170, (int) Math.toDegrees(model.getTheta()),
                v -> { model.reset(Math.toRadians(v)); panel.syncGhost(); }));

        // Integrator toggle
        JPanel methodPanel = new JPanel();
        methodPanel.setBackground(BG);
        JLabel methodLabel = styledLabel("Integrator:");
        JRadioButton euler = styledRadio("Euler");
        JRadioButton rk4   = styledRadio("RK4");
        rk4.setSelected(true);
        ButtonGroup bg = new ButtonGroup();
        bg.add(euler); bg.add(rk4);
        euler.addActionListener(e -> model.setMethod(Integrator.Method.EULER));
        rk4.addActionListener(e -> model.setMethod(Integrator.Method.RK4));
        methodPanel.add(methodLabel); methodPanel.add(euler); methodPanel.add(rk4);
        add(methodPanel);

        // Checkboxes
        JCheckBox trailBox = styledCheck("Trail",  true);
        JCheckBox ghostBox = styledCheck("Ghost",  true);
        trailBox.addActionListener(e -> panel.showTrail = trailBox.isSelected());
        ghostBox.addActionListener(e -> { panel.showGhost = ghostBox.isSelected(); panel.syncGhost(); });
        add(trailBox); add(ghostBox);

        // Reset
        JButton reset = new JButton("Reset");
        reset.setForeground(FG); reset.setBackground(new Color(40, 50, 80));
        reset.setFocusPainted(false);
        reset.addActionListener(e -> { model.reset(model.getTheta()); panel.syncGhost(); });
        add(reset);
    }

    private JPanel namedSlider(String label, int min, int max, int value, Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(155, 58));

        JLabel lbl = styledLabel(label + ": " + value);
        JSlider slider = new JSlider(min, max, value);
        slider.setBackground(BG);
        slider.setForeground(FG);

        slider.addChangeListener(e -> {
            lbl.setText(label + ": " + slider.getValue());
            onChange.accept(slider.getValue());
        });
        p.add(lbl,    BorderLayout.NORTH);
        p.add(slider, BorderLayout.CENTER);
        return p;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    private JRadioButton styledRadio(String text) {
        JRadioButton b = new JRadioButton(text);
        b.setBackground(BG); b.setForeground(FG);
        b.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return b;
    }

    private JCheckBox styledCheck(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setBackground(BG); cb.setForeground(FG);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return cb;
    }
}
