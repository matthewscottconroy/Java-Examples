package com.gameoflife.wolfram;

import com.gameoflife.wolfram.ui.WolframFrame;

import javax.swing.*;

/**
 * Entry point for the Wolfram 1D elementary cellular automata explorer.
 *
 * <p>Run with: {@code mvn exec:java@wolfram}
 */
public final class WolframMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WolframFrame().setVisible(true));
    }
}
