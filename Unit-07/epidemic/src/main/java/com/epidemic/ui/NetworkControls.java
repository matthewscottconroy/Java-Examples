package com.epidemic.ui;

import com.epidemic.model.SIRNetwork;
import com.epidemic.model.WattsStrogatz;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Control strip for the agent-based Network SIR simulation.
 *
 * <p>Provides sliders for the transmission rate (β), recovery rate (γ),
 * network size (N), mean degree (k), and rewiring probability (p).
 * A <em>Reset</em> button reinitialises the epidemic on the existing graph;
 * a <em>Rebuild</em> button constructs a new graph from the current parameters
 * and resets the epidemic.  A speed selector controls how many simulation steps
 * are executed per rendered frame.
 */
class NetworkControls extends JPanel {

    private static final Color BG = new Color(15, 17, 30);
    private static final Color FG = new Color(200, 210, 230);

    private final NetworkPanel panel;

    // Live parameters
    private double beta  = 0.30;
    private double gamma = 0.05;
    private int    nodeN = 100;
    private int    k     = 6;
    private double p     = 0.15;

    // Model reference — updated on Rebuild
    private SIRNetwork model;

    NetworkControls(SIRNetwork model, NetworkPanel panel) {
        this.model = model;
        this.panel = panel;

        setBackground(BG);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        // β slider  (0.05 … 0.80, step 0.01, display ×100)
        add(namedSlider("β (×100)", 5, 80, (int)(beta * 100), v -> {
            beta = v / 100.0;
            panel.setBeta(beta);
        }));

        // γ slider  (0.01 … 0.30, step 0.01, display ×100)
        add(namedSlider("γ (×100)", 1, 30, (int)(gamma * 100), v -> {
            gamma = v / 100.0;
            panel.setGamma(gamma);
        }));

        // N slider  (50 … 200)
        add(namedSlider("N nodes", 50, 200, nodeN, v -> nodeN = v));

        // k slider  (2 … 10, even only — snap in listener)
        add(namedSlider("k degree", 2, 10, k, v -> k = (v % 2 == 0) ? v : v - 1));

        // p slider  (0 … 50, display ×100 → 0.00 … 0.50)
        add(namedSlider("p rewire (×100)", 0, 50, (int)(p * 100), v -> p = v / 100.0));

        // Speed selector
        JPanel speedPanel = new JPanel();
        speedPanel.setBackground(BG);
        speedPanel.add(styledLabel("Speed:"));
        String[] speeds = {"1×", "2×", "5×", "10×"};
        int[]    vals   = {1, 2, 5, 10};
        JComboBox<String> speedBox = new JComboBox<>(speeds);
        speedBox.setBackground(BG);
        speedBox.setForeground(FG);
        speedBox.addActionListener(e -> panel.stepsPerFrame = vals[speedBox.getSelectedIndex()]);
        speedPanel.add(speedBox);
        add(speedPanel);

        // Pause toggle
        JButton pauseBtn = styledButton("Pause");
        pauseBtn.addActionListener(e -> {
            panel.paused = !panel.paused;
            pauseBtn.setText(panel.paused ? "Resume" : "Pause");
        });
        add(pauseBtn);

        // Reset — keep graph, reseed epidemic
        JButton resetBtn = styledButton("Reset");
        resetBtn.addActionListener(e -> model.reset(3, new Random()));
        add(resetBtn);

        // Rebuild — new graph + new epidemic
        JButton rebuildBtn = styledButton("Rebuild");
        rebuildBtn.addActionListener(e -> {
            int kEven = (k % 2 == 0) ? k : Math.max(2, k - 1);
            WattsStrogatz newGraph = WattsStrogatz.build(nodeN, kEven, p, System.nanoTime());
            this.model = new SIRNetwork(newGraph);
            this.model.reset(3, new Random());
            panel.setModel(this.model);
        });
        add(rebuildBtn);
    }

    // -------------------------------------------------------------------------
    // Widget helpers
    // -------------------------------------------------------------------------

    private JPanel namedSlider(String label, int min, int max, int value, Consumer<Integer> onChange) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(BG);
        p.setPreferredSize(new Dimension(140, 58));

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
