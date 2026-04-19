package com.epidemic.ui;

import com.epidemic.model.SIROde;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the aggregate ODE SIR simulation.
 *
 * <p>Provides sliders for the transmission rate (β), recovery rate (γ),
 * and the initial infected fraction (I₀/N).  A <em>Reset</em> button
 * reinitialises the model and clears the scrolling history.  A <em>Pause</em>
 * button toggles the animation.
 */
class OdeControls extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final SIROde   model;
    private final OdePanel panel;

    // Live parameters (mirrored from sliders)
    private double beta     = 0.30;
    private double gamma    = 0.05;
    private int    i0Frac   = 10;   // initial infected as % of N

    OdeControls(SIROde model, OdePanel panel) {
        this.model = model;
        this.panel = panel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // β slider (5 … 80, display ×100)
        add(namedSlider("β (×100)", 5, 80, (int)(beta * 100), v -> {
            beta = v / 100.0;
            panel.setBeta(beta);
        }));

        // γ slider (1 … 30, display ×100)
        add(namedSlider("γ (×100)", 1, 30, (int)(gamma * 100), v -> {
            gamma = v / 100.0;
            panel.setGamma(gamma);
        }));

        // Initial infected % (1 … 50)
        add(namedSlider("I₀ (% of N)", 1, 50, i0Frac, v -> i0Frac = v));

        // Pause toggle
        JButton pauseBtn = styledButton("Pause");
        pauseBtn.addActionListener(e -> {
            panel.paused = !panel.paused;
            pauseBtn.setText(panel.paused ? "Resume" : "Pause");
        });
        add(pauseBtn);

        // Reset
        JButton resetBtn = styledButton("Reset");
        resetBtn.addActionListener(e -> {
            double i0 = model.getN() * i0Frac / 100.0;
            model.reset(i0);
            panel.resetHistory();
        });
        add(resetBtn);
    }

    // -------------------------------------------------------------------------
    // Widget helpers
    // -------------------------------------------------------------------------

    private JPanel namedSlider(String label, int min, int max, int value, Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(160, 58));

        JLabel  lbl    = styledLabel(label + ": " + value);
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

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(FG);
        b.setBackground(new Color(40, 50, 80));
        b.setFocusPainted(false);
        return b;
    }
}
