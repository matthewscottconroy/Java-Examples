package com.pendulums.ui;

import com.pendulums.model.DoublePendulum;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the double pendulum simulation.
 */
class DoublePendulumControls extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    DoublePendulumControls(DoublePendulum model, DoublePendulumPanel panel) {
        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        add(namedSlider("θ₁ (°)", -170, 170, (int) Math.toDegrees(model.getTheta1()),
                v -> { model.setTheta1(Math.toRadians(v)); model.reset(model.getTheta1(), model.getTheta2()); panel.resetShadow(); }));

        add(namedSlider("θ₂ (°)", -170, 170, (int) Math.toDegrees(model.getTheta2()),
                v -> { model.setTheta2(Math.toRadians(v)); model.reset(model.getTheta1(), model.getTheta2()); panel.resetShadow(); }));

        add(namedSlider("L₁ (px)", 40, 220, (int) model.getLength1(),
                v -> { model.setLength1(v); model.reset(model.getTheta1(), model.getTheta2()); panel.resetShadow(); }));

        add(namedSlider("L₂ (px)", 40, 220, (int) model.getLength2(),
                v -> { model.setLength2(v); model.reset(model.getTheta1(), model.getTheta2()); panel.resetShadow(); }));

        add(namedSlider("m₁ (×0.1)", 1, 50, (int)(model.getMass1() * 10),
                v -> { model.setMass1(v / 10.0); model.reset(model.getTheta1(), model.getTheta2()); panel.resetShadow(); }));

        add(namedSlider("m₂ (×0.1)", 1, 50, (int)(model.getMass2() * 10),
                v -> { model.setMass2(v / 10.0); model.reset(model.getTheta1(), model.getTheta2()); panel.resetShadow(); }));

        add(namedSlider("Gravity (px/s²)", 100, 2000, (int) model.getGravity(),
                v -> model.setGravity(v)));

        JCheckBox trailBox  = styledCheck("Trail",  true);
        JCheckBox shadowBox = styledCheck("Shadow", false);
        trailBox.addActionListener(e  -> panel.showTrail  = trailBox.isSelected());
        shadowBox.addActionListener(e -> { panel.showShadow = shadowBox.isSelected(); panel.resetShadow(); });
        add(trailBox); add(shadowBox);

        JButton reset = new JButton("Reset");
        reset.setForeground(FG); reset.setBackground(new Color(40, 50, 80));
        reset.setFocusPainted(false);
        reset.addActionListener(e -> { model.reset(model.getTheta1(), model.getTheta2()); panel.resetShadow(); });
        add(reset);
    }

    private JPanel namedSlider(String label, int min, int max, int value, Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(140, 58));
        JLabel lbl = styledLabel(label + ": " + value);
        JSlider slider = new JSlider(min, max, value);
        slider.setBackground(BG); slider.setForeground(FG);
        slider.addChangeListener(e -> { lbl.setText(label + ": " + slider.getValue()); onChange.accept(slider.getValue()); });
        p.add(lbl, BorderLayout.NORTH); p.add(slider, BorderLayout.CENTER);
        return p;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG); l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    private JCheckBox styledCheck(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setBackground(BG); cb.setForeground(FG);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return cb;
    }
}
