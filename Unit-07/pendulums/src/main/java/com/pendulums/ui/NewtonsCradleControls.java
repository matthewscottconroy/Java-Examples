package com.pendulums.ui;

import com.pendulums.model.NewtonsCradle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the Newton's Cradle simulation.
 */
class NewtonsCradleControls extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final NewtonsCradle      model;
    private final NewtonsCradlePanel panel;

    // Tracks current lift count and angle for the reset button
    private int    liftCount = 1;
    private double liftAngle = Math.toRadians(55);

    NewtonsCradleControls(NewtonsCradle model, NewtonsCradlePanel panel) {
        this.model = model;
        this.panel = panel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // Ball count spinner
        JPanel countPanel = new JPanel(new BorderLayout(0, 2));
        countPanel.setBackground(BG);
        countPanel.setPreferredSize(new Dimension(120, 58));
        JLabel countLabel = styledLabel("Balls: " + model.getBallCount());
        SpinnerModel sm   = new SpinnerNumberModel(model.getBallCount(),
                                                   NewtonsCradle.MIN_BALLS,
                                                   NewtonsCradle.MAX_BALLS, 1);
        JSpinner countSpin = new JSpinner(sm);
        countSpin.setBackground(BG); countSpin.setForeground(FG);
        countSpin.addChangeListener(e -> {
            int n = (int) countSpin.getValue();
            countLabel.setText("Balls: " + n);
            liftCount = Math.min(liftCount, n - 1);
            model.setBallCount(n);
            panel.recomputeGeometry();
            model.reset(liftCount, liftAngle);
        });
        countPanel.add(countLabel, BorderLayout.NORTH);
        countPanel.add(countSpin,  BorderLayout.CENTER);
        add(countPanel);

        // Lift count
        add(namedSlider("Lift", 1, 4, liftCount, v -> {
            liftCount = v;
            model.reset(liftCount, liftAngle);
        }));

        // Lift angle
        add(namedSlider("Angle (°)", 10, 170, (int) Math.toDegrees(liftAngle), v -> {
            liftAngle = Math.toRadians(v);
            model.reset(liftCount, liftAngle);
        }));

        // Restitution
        add(namedSlider("Restitution (×0.01)", 0, 100, (int)(model.getRestitution() * 100), v -> {
            model.setRestitution(v / 100.0);
        }));

        // Gravity
        add(namedSlider("Gravity (px/s²)", 100, 2000, (int) model.getGravity(), v -> {
            model.setGravity(v);
        }));

        JButton reset = new JButton("Reset");
        reset.setForeground(FG); reset.setBackground(new Color(40, 50, 80));
        reset.setFocusPainted(false);
        reset.addActionListener(e -> model.reset(liftCount, liftAngle));
        add(reset);

        JButton allStop = new JButton("Stop All");
        allStop.setForeground(FG); allStop.setBackground(new Color(40, 50, 80));
        allStop.setFocusPainted(false);
        allStop.addActionListener(e -> model.resetAll());
        add(allStop);
    }

    private JPanel namedSlider(String label, int min, int max, int value, Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(160, 58));
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
}
