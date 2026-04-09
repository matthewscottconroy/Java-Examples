package com.schelling;

import com.schelling.model.SimulationConfig;
import com.schelling.ui.MainFrame;

import javax.swing.*;

/**
 * Entry point for the Schelling Segregation Simulation application.
 *
 * <p>Starts the Swing event-dispatch thread and opens {@link MainFrame}
 * with the default simulation configuration.
 */
public final class Main {

    private Main() { /* utility class */ }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        // Use system look-and-feel for native widget rendering
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall through to default Swing LAF
        }

        SwingUtilities.invokeLater(() ->
            new MainFrame(SimulationConfig.defaults())
        );
    }
}
