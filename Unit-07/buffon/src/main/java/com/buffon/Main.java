package com.buffon;

import com.buffon.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Buffon's Needle simulator.
 *
 * <p>Launches the interactive experiment window where needles are dropped
 * onto a ruled floor and the resulting crossing frequency is used to
 * estimate π via Monte Carlo integration.
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
