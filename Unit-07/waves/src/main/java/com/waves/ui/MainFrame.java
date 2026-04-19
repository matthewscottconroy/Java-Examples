package com.waves.ui;

import com.waves.model.WaveMembrane;
import com.waves.model.WaveString;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window.
 *
 * <p>Hosts two simulation tabs in a {@link JTabbedPane}; each tab holds a
 * simulation canvas (CENTER) and a parameter-control strip (SOUTH).
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Wave Propagation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(20, 22, 36));
        tabs.setForeground(Color.WHITE);

        tabs.addTab("1D String",    buildStringTab());
        tabs.addTab("2D Membrane",  buildMembraneTab());

        setContentPane(tabs);
        pack();
        setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // Tab builders
    // -------------------------------------------------------------------------

    private JComponent buildStringTab() {
        WaveString model = new WaveString();
        // Start with a pluck near the centre
        model.pluck(WaveString.N / 3, 0.6, 20.0);

        StringPanel   canvas = new StringPanel(model);
        StringControls ctrl  = new StringControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }

    private JComponent buildMembraneTab() {
        WaveMembrane model = new WaveMembrane();
        // Start with a poke near the centre
        model.poke(WaveMembrane.COLS / 2, WaveMembrane.ROWS / 2, 1.0, 8.0);

        MembranePanel   canvas = new MembranePanel(model);
        MembraneControls ctrl  = new MembraneControls(model, canvas);

        JPanel tab = new JPanel(new BorderLayout());
        tab.setBackground(new Color(8, 10, 22));
        tab.add(canvas, BorderLayout.CENTER);
        tab.add(ctrl,   BorderLayout.SOUTH);
        return tab;
    }
}
