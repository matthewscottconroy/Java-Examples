package com.boids;

import com.boids.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Boids — Emergent Flocking Simulation.
 *
 * <p>Launches a single window containing the interactive flock simulation
 * based on Craig Reynolds' 1987 Boids model.  Three steering rules —
 * separation, alignment, and cohesion — produce realistic emergent flocking
 * from simple local interactions.
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
