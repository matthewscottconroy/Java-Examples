package com.buoyancy.equation;

import com.buoyancy.equation.ui.MainFrame;
import javax.swing.SwingUtilities;

/** Entry point for the Archimedes equation-based buoyancy simulator. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
