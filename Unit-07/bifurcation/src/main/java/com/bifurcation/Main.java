package com.bifurcation;

import com.bifurcation.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Bifurcation Theory simulator.
 *
 * <p>Launches a tabbed window containing two interactive simulations:
 * the Logistic Map bifurcation diagram and the Lorenz Attractor.
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
