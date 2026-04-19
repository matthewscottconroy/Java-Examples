package com.epidemic.ui;

import com.epidemic.model.SIROde;
import com.epidemic.model.SIRNetwork;
import com.epidemic.model.WattsStrogatz;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Top-level application window for the SIR Epidemic Simulator.
 *
 * <p>Hosts two simulation tabs in a {@link JTabbedPane}:
 * <ul>
 *   <li><b>Network SIR</b> — agent-based model on a Watts-Strogatz small-world graph</li>
 *   <li><b>ODE SIR</b> — aggregate compartmental model integrated with RK4</li>
 * </ul>
 * Each tab holds a simulation canvas (CENTER) and a parameter-control strip (SOUTH).
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("SIR Epidemic Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(20, 22, 36));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("Network SIR", buildNetworkTab());
        tabs.addTab("ODE SIR",     buildOdeTab());

        setContentPane(tabs);
        pack();
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // Tab builders
    // -------------------------------------------------------------------------

    private JComponent buildNetworkTab() {
        WattsStrogatz graph   = WattsStrogatz.build(100, 6, 0.15, 42L);
        SIRNetwork    model   = new SIRNetwork(graph);
        model.reset(3, new Random(7L));

        NetworkPanel    canvas = new NetworkPanel(model);
        NetworkControls ctrl   = new NetworkControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }

    private JComponent buildOdeTab() {
        SIROde model = new SIROde(SIROde.DEFAULT_N, SIROde.DEFAULT_I0);

        OdePanel    canvas = new OdePanel(model);
        OdeControls ctrl   = new OdeControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }
}
