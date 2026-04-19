package com.waves;

import com.waves.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Wave Propagation simulator.
 *
 * <p>Launches a tabbed window containing two interactive simulations:
 * a 1D vibrating string and a 2D membrane, both solved via
 * finite-difference wave equations.
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
