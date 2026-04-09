package com.buoyancy.pressure;

import com.buoyancy.pressure.ui.MainFrame;
import javax.swing.SwingUtilities;

/** Entry point for the pressure-differential buoyancy simulator. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
