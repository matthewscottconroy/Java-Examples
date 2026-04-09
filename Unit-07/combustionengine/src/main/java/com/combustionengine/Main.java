package com.combustionengine;

import com.combustionengine.ui.MainFrame;
import javax.swing.SwingUtilities;

/** Entry point for the Otto-cycle combustion engine simulator. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
