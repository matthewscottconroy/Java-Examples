package com.pendulums;

import com.pendulums.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Pendulum Dynamics simulator.
 *
 * <p>Launches a tabbed window containing three interactive simulations:
 * a simple pendulum, a chaotic double pendulum, and Newton's Cradle.
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
