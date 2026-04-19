package com.epidemic;

import com.epidemic.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the SIR Epidemic Simulator.
 *
 * <p>Launches a tabbed window containing two interactive simulations:
 * an agent-based SIR model on a Watts-Strogatz small-world network,
 * and an aggregate ODE-based SIR model integrated with RK4.
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
