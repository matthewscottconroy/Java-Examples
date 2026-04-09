package com.projectiles;

import javax.swing.*;

/** Entry point. */
public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
    }
}
