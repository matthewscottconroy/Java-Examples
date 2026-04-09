package com.gameoflife.gol;

import com.gameoflife.gol.ui.GameFrame;

import javax.swing.*;

/**
 * Entry point for Conway's Game of Life simulator.
 *
 * <p>Run with: {@code mvn exec:java}
 */
public final class Main {
    public static void main(String[] args) {
        // Use system look and feel where available, fall back to default dark
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
    }
}
