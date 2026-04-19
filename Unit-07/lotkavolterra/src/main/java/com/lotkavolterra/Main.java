package com.lotkavolterra;

import com.lotkavolterra.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Lotka-Volterra Predator-Prey Dynamics simulator.
 *
 * <p>Launches a tabbed window containing an interactive simulation with a
 * time-series chart and a phase portrait, driven by the classical
 * Lotka-Volterra equations of predator-prey population dynamics.
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
