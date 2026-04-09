package com.orbitaldynamics.twobody;

import com.orbitaldynamics.twobody.ui.TwoBodyFrame;

import javax.swing.*;

/**
 * Entry point for the two-body analytical orbit solver.
 *
 * <p>Run with: {@code mvn exec:java -Ptwobody}
 */
public final class TwoBodyMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TwoBodyFrame().setVisible(true));
    }
}
