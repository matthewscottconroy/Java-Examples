package com.lotkavolterra.ui;

import com.lotkavolterra.model.LotkaVolterra;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window for the Lotka-Volterra simulator.
 *
 * <p>Hosts two simulation tabs in a {@link JTabbedPane}: a scrolling time-series
 * chart and a phase-portrait plot.  Both tabs share the same {@link LotkaVolterra}
 * model instance so they remain synchronised.  A control strip below each tab
 * provides parameter sliders and a reset button.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Lotka-Volterra Predator-Prey Dynamics");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Shared model — default initial populations
        LotkaVolterra model = new LotkaVolterra(10.0, 5.0);

        // Build panels
        TimeSeriesPanel   timeSeries   = new TimeSeriesPanel(model);
        PhasePortraitPanel phasePortrait = new PhasePortraitPanel(model);
        ControlPanel      controls      = new ControlPanel(model, timeSeries, phasePortrait);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(20, 22, 36));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("Time Series",    buildTab(timeSeries));
        tabs.addTab("Phase Portrait", buildTab(phasePortrait));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(8, 10, 22));
        root.add(tabs,     BorderLayout.CENTER);
        root.add(controls, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // Tab builders
    // -------------------------------------------------------------------------

    private JComponent buildTab(JPanel canvas) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        return tab;
    }
}
