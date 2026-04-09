package com.bouncingballs;

import com.bouncingballs.ui.MainFrame;
import javax.swing.SwingUtilities;

/** Entry point for the Glass Jar — Bouncing Balls simulator. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
