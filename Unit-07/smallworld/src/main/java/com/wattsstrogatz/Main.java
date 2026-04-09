package com.wattsstrogatz;

import com.wattsstrogatz.model.NetworkConfig;
import com.wattsstrogatz.ui.MainFrame;

import javax.swing.*;

/**
 * Entry point for the Watts-Strogatz Small-World Simulation.
 *
 * <p>Launches the Swing UI on the event-dispatch thread with default
 * parameters (n=60, k=3, p=0.05).
 */
public final class Main {

    private Main() {}

    /**
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainFrame(NetworkConfig.defaults()));
    }
}
