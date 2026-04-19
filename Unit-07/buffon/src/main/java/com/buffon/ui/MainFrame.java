package com.buffon.ui;

import com.buffon.model.BuffonExperiment;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window for the Buffon's Needle simulator.
 *
 * <p>Hosts the simulation canvas (CENTER) and the parameter control strip (SOUTH).
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Buffon's Needle — Monte Carlo Estimation of π");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        BuffonExperiment experiment = new BuffonExperiment(100.0, 70.0);

        SimulationPanel sim  = new SimulationPanel(experiment);
        ControlPanel    ctrl = new ControlPanel(experiment, sim);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(8, 10, 22));
        content.add(sim,  BorderLayout.CENTER);
        content.add(ctrl, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }
}
