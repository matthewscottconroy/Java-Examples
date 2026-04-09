package com.orbitaldynamics.sim;

import com.orbitaldynamics.sim.ui.SimulationFrame;

import javax.swing.*;

/**
 * Entry point for the N-body orbital dynamics simulator.
 */
public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulationFrame().setVisible(true));
    }
}
