package com.bifurcation.ui;

import com.bifurcation.model.LogisticMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Control strip for the logistic map bifurcation diagram.
 *
 * <p>Provides sliders for the r-range boundaries (rMin and rMax), spinners
 * for the transient and attractor iteration counts, and a Reset/Recompute button
 * that rebuilds the pre-rendered diagram image with the current settings.
 */
class BifurcationControls extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final LogisticMap       model;
    private final BifurcationPanel  panel;

    BifurcationControls(LogisticMap model, BifurcationPanel panel) {
        this.model = model;
        this.panel = panel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // rMin slider [2.0, 3.5] in steps of 0.01 → scale by 100
        add(namedSlider("r min (×0.01)", 200, 350, (int)(panel.rMin * 100),
                v -> {
                    double val = v / 100.0;
                    if (val < panel.rMax - 0.05) { panel.rMin = val; }
                }));

        // rMax slider [2.5, 4.0]
        add(namedSlider("r max (×0.01)", 250, 400, (int)(panel.rMax * 100),
                v -> {
                    double val = v / 100.0;
                    if (val > panel.rMin + 0.05) { panel.rMax = val; }
                }));

        // Transient spinner
        SpinnerNumberModel transientModel = new SpinnerNumberModel(
                panel.transientSteps, 50, 5000, 50);
        JSpinner transientSpinner = styledSpinner(transientModel);
        transientSpinner.addChangeListener(e ->
                panel.transientSteps = (int) transientSpinner.getValue());
        add(labeledComponent("Transient steps:", transientSpinner));

        // Attractor spinner
        SpinnerNumberModel attractorModel = new SpinnerNumberModel(
                panel.attractorSteps, 50, 2000, 50);
        JSpinner attractorSpinner = styledSpinner(attractorModel);
        attractorSpinner.addChangeListener(e ->
                panel.attractorSteps = (int) attractorSpinner.getValue());
        add(labeledComponent("Attractor steps:", attractorSpinner));

        // Recompute button
        JButton recompute = new JButton("Recompute");
        recompute.setForeground(FG);
        recompute.setBackground(new Color(40, 50, 80));
        recompute.setFocusPainted(false);
        recompute.addActionListener(e -> panel.recompute());
        add(recompute);

        // Reset to defaults button
        JButton reset = new JButton("Reset");
        reset.setForeground(FG);
        reset.setBackground(new Color(40, 50, 80));
        reset.setFocusPainted(false);
        reset.addActionListener(e -> {
            panel.rMin = 2.4;
            panel.rMax = 4.0;
            panel.transientSteps = 500;
            panel.attractorSteps = 200;
            panel.recompute();
        });
        add(reset);
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
            panel.recompute();
        });
        p.add(lbl,    BorderLayout.NORTH);
        p.add(slider, BorderLayout.CENTER);
        return p;
    }

    private JPanel labeledComponent(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.add(styledLabel(label));
        p.add(comp);
        return p;
    }

    private JSpinner styledSpinner(SpinnerNumberModel m) {
        JSpinner s = new JSpinner(m);
        s.setPreferredSize(new Dimension(80, 24));
        s.setBackground(BG);
        s.setForeground(FG);
        return s;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(FG);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }
}
