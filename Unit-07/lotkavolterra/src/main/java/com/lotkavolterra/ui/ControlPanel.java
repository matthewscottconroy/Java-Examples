package com.lotkavolterra.ui;

import com.lotkavolterra.model.LotkaVolterra;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Parameter-control strip for the Lotka-Volterra simulator.
 *
 * <p>Provides labelled sliders for all four Lotka-Volterra parameters (α, β, δ, γ),
 * initial population sliders for x₀ and y₀, an optional harvesting slider, and
 * a Reset button that restores the current initial conditions.
 *
 * <p>All parameter changes take effect immediately on the running simulation.
 * Slider labels display the current value in two decimal places.
 */
class ControlPanel extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final LotkaVolterra    model;
    private final TimeSeriesPanel  timeSeries;
    private final PhasePortraitPanel phasePortrait;

    // Cached initial-population values so reset knows what to use
    private double x0 = 10.0;
    private double y0 =  5.0;

    /**
     * Construct the control panel.
     *
     * @param model        the shared Lotka-Volterra model
     * @param timeSeries   the time-series panel (cleared on reset)
     * @param phasePortrait the phase-portrait panel (cleared on reset)
     */
    ControlPanel(LotkaVolterra model,
                 TimeSeriesPanel timeSeries,
                 PhasePortraitPanel phasePortrait) {
        this.model         = model;
        this.timeSeries    = timeSeries;
        this.phasePortrait = phasePortrait;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        // --- Lotka-Volterra parameters ---
        add(floatSlider("\u03b1 (prey growth)",   10, 300, (int)(model.getAlpha()  * 100),
                v -> model.setAlpha(v / 100.0)));

        add(floatSlider("\u03b2 (predation)",      1, 100, (int)(model.getBeta()   * 100),
                v -> model.setBeta(v / 100.0)));

        add(floatSlider("\u03b4 (pred. growth)",   1, 100, (int)(model.getDelta()  * 100),
                v -> model.setDelta(v / 100.0)));

        add(floatSlider("\u03b3 (pred. death)",   10, 300, (int)(model.getGamma()  * 100),
                v -> model.setGamma(v / 100.0)));

        // --- Initial conditions ---
        add(intSlider("x\u2080 (prey init)",    1, 50, (int) x0, v -> x0 = v));
        add(intSlider("y\u2080 (pred. init)",   1, 50, (int) y0, v -> y0 = v));

        // --- Harvesting ---
        add(floatSlider("H (harvest)",   0, 50, 0,
                v -> model.setHarvesting(v / 100.0)));

        // --- Reset button ---
        JButton reset = new JButton("Reset");
        reset.setForeground(FG);
        reset.setBackground(new Color(40, 50, 80));
        reset.setFocusPainted(false);
        reset.addActionListener(e -> {
            model.reset(x0, y0);
            timeSeries.clearHistory();
            phasePortrait.clearTrail();
        });
        add(reset);
    }

    // -------------------------------------------------------------------------
    // Slider helpers
    // -------------------------------------------------------------------------

    /**
     * Create a labelled integer slider.
     *
     * @param label    display name (shown above the slider)
     * @param min      slider minimum (integer)
     * @param max      slider maximum (integer)
     * @param value    initial value
     * @param onChange callback invoked with the raw integer slider value
     * @return a panel containing the label and slider
     */
    private JPanel intSlider(String label, int min, int max, int value,
                             Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(140, 55));

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

    /**
     * Create a labelled slider whose raw integer values are divided by 100 before
     * being passed to {@code onChange}, giving two decimal places of precision.
     *
     * @param label    display name
     * @param min      slider minimum (×100 of the real value)
     * @param max      slider maximum (×100 of the real value)
     * @param value    initial slider integer value
     * @param onChange callback invoked with the real (divided-by-100) value
     * @return a panel containing the label and slider
     */
    private JPanel floatSlider(String label, int min, int max, int value,
                               Consumer<Double> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(140, 55));

        JLabel  lbl    = styledLabel(String.format("%s: %.2f", label, value / 100.0));
        JSlider slider = new JSlider(min, max, value);
        slider.setBackground(BG);
        slider.setForeground(FG);
        slider.addChangeListener(e -> {
            double real = slider.getValue() / 100.0;
            lbl.setText(String.format("%s: %.2f", label, real));
            onChange.accept(real);
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
