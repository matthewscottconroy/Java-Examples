package com.bifurcation.ui;

import com.bifurcation.model.LogisticMap;
import com.bifurcation.model.LorenzAttractor;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window.
 *
 * <p>Hosts two simulation tabs in a {@link JTabbedPane}: the Logistic Map
 * bifurcation diagram and the Lorenz Attractor. Each tab holds a simulation
 * canvas ({@code CENTER}) and a parameter-control strip ({@code SOUTH}).
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Bifurcation Theory");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(20, 22, 36));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("Logistic Map",      buildLogisticTab());
        tabs.addTab("Lorenz Attractor",  buildLorenzTab());

        setContentPane(tabs);
        pack();
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // Tab builders
    // -------------------------------------------------------------------------

    private JComponent buildLogisticTab() {
        LogisticMap model = new LogisticMap(3.5);

        BifurcationPanel   canvas = new BifurcationPanel(model);
        BifurcationControls ctrl  = new BifurcationControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }

    private JComponent buildLorenzTab() {
        // Classic parameters: sigma=10, rho=28, beta=8/3
        LorenzAttractor model = new LorenzAttractor(0.1, 0.0, 0.0, 10.0, 28.0, 8.0 / 3.0);

        LorenzPanel    canvas = new LorenzPanel(model);
        LorenzControls ctrl   = new LorenzControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }
}
