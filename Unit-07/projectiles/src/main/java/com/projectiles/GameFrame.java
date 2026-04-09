package com.projectiles;

import javax.swing.*;

/** Application window for the projectile simulator. */
public final class GameFrame extends JFrame {

    public GameFrame() {
        super("Slingshot — Destructible Terrain");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);

        // Give focus to panel so key events work immediately
        panel.requestFocusInWindow();
    }
}
