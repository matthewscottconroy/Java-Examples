package com.boids.ui;

import com.boids.model.FlockSimulation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Parameter control strip for the Boids simulation.
 *
 * <p>Provides labelled sliders for all major simulation parameters:
 *
 * <ul>
 *   <li>Boid count (10 – 300)</li>
 *   <li>Perception radius (20 – 200 px)</li>
 *   <li>Separation weight (0 – 3.0, slider × 0.1)</li>
 *   <li>Alignment weight  (0 – 3.0, slider × 0.1)</li>
 *   <li>Cohesion weight   (0 – 3.0, slider × 0.1)</li>
 *   <li>Max speed (20 – 300 px/s)</li>
 * </ul>
 *
 * <p>A <em>Reset</em> button respawns the flock with the current boid count,
 * and a <em>Clear Predator</em> button removes any active predator.
 */
class ControlPanel extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final FlockSimulation sim;
    private final FlockPanel      canvas;

    /**
     * Construct the control panel.
     *
     * @param sim    the simulation model to control
     * @param canvas the rendering panel (needed to access boid count live)
     */
    ControlPanel(FlockSimulation sim, FlockPanel canvas) {
        this.sim    = sim;
        this.canvas = canvas;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // Boid count slider (triggers a full reset of the flock)
        add(namedSlider("Boid count", 10, 300, sim.getBoidCount(),
                v -> { sim.setBoidCount(v); sim.reset(v); }));

        // Perception radius
        add(namedSlider("Perception r (px)", 20, 200, (int) sim.getPerceptionRadius(),
                v -> sim.setPerceptionRadius(v)));

        // Separation weight  ×0.1
        add(namedSliderDecimal("Sep weight (×0.1)", 0, 30,
                (int) Math.round(sim.getSeparationWeight() * 10),
                v -> sim.setSeparationWeight(v / 10.0)));

        // Alignment weight  ×0.1
        add(namedSliderDecimal("Align weight (×0.1)", 0, 30,
                (int) Math.round(sim.getAlignmentWeight() * 10),
                v -> sim.setAlignmentWeight(v / 10.0)));

        // Cohesion weight  ×0.1
        add(namedSliderDecimal("Cohesion weight (×0.1)", 0, 30,
                (int) Math.round(sim.getCohesionWeight() * 10),
                v -> sim.setCohesionWeight(v / 10.0)));

        // Max speed
        add(namedSlider("Max speed (px/s)", 20, 300, (int) sim.getMaxSpeed(),
                v -> sim.setMaxSpeed(v)));

        // Reset button
        JButton reset = styledButton("Reset");
        reset.addActionListener(e -> sim.reset(sim.getBoidCount()));
        add(reset);

        // Clear predator button
        JButton clearPred = styledButton("Clear Predator");
        clearPred.addActionListener(e -> sim.clearPredator());
        add(clearPred);
    }

    // -------------------------------------------------------------------------
    // Widget factories
    // -------------------------------------------------------------------------

    /** Slider that shows its integer value in the label. */
    private JPanel namedSlider(String label, int min, int max, int value,
                                Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(155, 58));

        JLabel lbl    = styledLabel(label + ": " + value);
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

    /** Slider that shows its value / 10.0 in the label (one decimal place). */
    private JPanel namedSliderDecimal(String label, int min, int max, int value,
                                       Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(160, 58));

        JLabel lbl    = styledLabel(label + ": " + String.format("%.1f", value / 10.0));
        JSlider slider = new JSlider(min, max, value);
        slider.setBackground(BG);
        slider.setForeground(FG);

        slider.addChangeListener(e -> {
            lbl.setText(label + ": " + String.format("%.1f", slider.getValue() / 10.0));
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

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(FG);
        b.setBackground(new Color(40, 50, 80));
        b.setFocusPainted(false);
        return b;
    }
}
