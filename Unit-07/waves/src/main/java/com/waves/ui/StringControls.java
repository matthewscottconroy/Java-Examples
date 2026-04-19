package com.waves.ui;

import com.waves.model.WaveString;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the 1D string simulation.
 *
 * <p>Provides sliders for wave speed, damping, and pluck amplitude, plus a
 * Reset button that clears the string displacement.
 */
class StringControls extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final WaveString  model;
    private final StringPanel panel;

    StringControls(WaveString model, StringPanel panel) {
        this.model = model;
        this.panel = panel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // Wave speed slider: 50 – 800 px/s
        add(namedSlider("Wave Speed (px/s)", 50, 800, (int) panel.waveSpeed,
                v -> panel.waveSpeed = v));

        // Damping slider: 0 – 50 mapped to 0.0 – 0.005
        add(namedSlider("Damping (×0.0001)", 0, 50, (int)(panel.damping * 10_000),
                v -> panel.damping = v / 10_000.0));

        // Amplitude slider: 10 – 100 mapped to 0.1 – 1.0
        add(namedSlider("Amplitude (×0.01)", 10, 100, (int)(panel.amplitude * 100),
                v -> panel.amplitude = v / 100.0));

        // Reset button
        JButton reset = new JButton("Reset");
        reset.setForeground(FG);
        reset.setBackground(new Color(40, 50, 80));
        reset.setFocusPainted(false);
        reset.addActionListener(e -> model.reset());
        add(reset);
    }

    private JPanel namedSlider(String label, int min, int max, int value,
                               Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(175, 58));

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
}
