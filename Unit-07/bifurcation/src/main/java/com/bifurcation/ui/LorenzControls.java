package com.bifurcation.ui;

import com.bifurcation.model.LorenzAttractor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the Lorenz attractor simulation.
 *
 * <p>Provides sliders for the three Lorenz parameters (σ, ρ, β), a Reset button
 * that restores the classic parameter values and clears all trail history, and
 * an informational label showing the default parameter values.
 *
 * <h2>Parameter ranges</h2>
 * <ul>
 *   <li>σ (sigma): 1 – 20, integer steps</li>
 *   <li>ρ (rho): 0.5 – 50, steps of 0.5 (slider in units of 0.5)</li>
 *   <li>β (beta): 0.1 – 5.0, steps of 0.1 (slider in units of 0.1)</li>
 * </ul>
 */
class LorenzControls extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private static final double DEFAULT_SIGMA = 10.0;
    private static final double DEFAULT_RHO   = 28.0;
    private static final double DEFAULT_BETA  = 8.0 / 3.0;

    private final LorenzAttractor model;
    private final LorenzPanel     panel;

    LorenzControls(LorenzAttractor model, LorenzPanel panel) {
        this.model = model;
        this.panel = panel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // σ slider [1, 20]
        add(namedSlider("σ (sigma)", 1, 20, (int) model.getSigma(),
                v -> model.setSigma(v)));

        // ρ slider [1, 100] representing [0.5, 50] in steps of 0.5
        add(namedSliderDouble("ρ (rho) ×0.5", 1, 100, (int)(model.getRho() * 2),
                v -> model.setRho(v / 2.0)));

        // β slider [1, 50] representing [0.1, 5.0] in steps of 0.1
        add(namedSliderDouble("β (beta) ×0.1", 1, 50, (int)(model.getBeta() * 10),
                v -> model.setBeta(v / 10.0)));

        // Reset button — restores classic parameters and clears trails
        JButton reset = new JButton("Reset");
        reset.setForeground(FG);
        reset.setBackground(new Color(40, 50, 80));
        reset.setFocusPainted(false);
        reset.addActionListener(e -> {
            model.setSigma(DEFAULT_SIGMA);
            model.setRho(DEFAULT_RHO);
            model.setBeta(DEFAULT_BETA);
            model.reset();
            panel.repaint();
        });
        add(reset);

        // Info label
        JLabel info = styledLabel(
                String.format("Classic params: σ=%.0f  ρ=%.0f  β=%.4f (=8/3)",
                        DEFAULT_SIGMA, DEFAULT_RHO, DEFAULT_BETA));
        info.setForeground(new Color(140, 160, 190));
        add(info);
    }

    private JPanel namedSlider(String label, int min, int max, int value,
                                Consumer<Integer> onChange) {
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

    private JPanel namedSliderDouble(String label, int min, int max, int value,
                                      Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(155, 58));

        JLabel lbl = styledLabel(label + ": " + value);
        JSlider slider = new JSlider(min, max, Math.max(min, Math.min(max, value)));
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
}
