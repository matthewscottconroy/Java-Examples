package com.boids.ui;

import com.boids.model.FlockSimulation;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window for the Boids flocking simulation.
 *
 * <p>Hosts a single simulation canvas ({@link FlockPanel}) in the CENTER and
 * a parameter control strip ({@link ControlPanel}) in the SOUTH of a
 * {@link BorderLayout}.  The window is non-resizable so pixel coordinates in
 * the simulation match the canvas dimensions exactly.
 */
public class MainFrame extends JFrame {

    /**
     * Construct and lay out the main application window.
     *
     * <p>A {@link FlockSimulation} is created at the canvas size
     * ({@value FlockPanel#W} × {@value FlockPanel#H} px) and shared between
     * the canvas and the control panel.
     */
    public MainFrame() {
        super("Boids — Emergent Flocking Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        FlockSimulation sim    = new FlockSimulation(FlockPanel.W, FlockPanel.H);
        FlockPanel      canvas = new FlockPanel(sim);
        ControlPanel    ctrl   = new ControlPanel(sim, canvas);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(8, 10, 22));
        content.add(canvas, BorderLayout.CENTER);
        content.add(ctrl,   BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }
}
