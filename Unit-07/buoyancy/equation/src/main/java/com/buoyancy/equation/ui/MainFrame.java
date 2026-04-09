package com.buoyancy.equation.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Application window for the Archimedes equation-based buoyancy simulator.
 *
 * <p>Layout: simulation canvas on the left, control panel on the right.
 * The control panel refreshes its metrics display every simulation tick.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Buoyancy — Archimedes Equation Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        SimulationPanel sim  = new SimulationPanel();
        ControlPanel    ctrl = new ControlPanel(sim);

        // Wire tick callback: control panel refreshes after each physics step
        sim.setOnTick(ctrl::refresh);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(new Color(20, 24, 38));
        content.add(sim,  BorderLayout.CENTER);
        content.add(ctrl, BorderLayout.EAST);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }
}
